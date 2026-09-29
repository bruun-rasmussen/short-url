job "short-url" {
  datacenters = ["BR1"]
  type        = "service"
  node_pool   = "production"

  group "short-url" {
    count = 1

    vault {
      role = "nomad-workloads"
    }

    network {
      port "http" {
        static = 4009
        to     = 4009
      }
    }

    task "short-url" {
      driver = "docker"

      config {
        image = "degas.bruun-rasmussen.dk:5000/osa/short-url:2.3"
        ports = ["http"]
      }

      env {
        TZ                        = "Europe/Copenhagen"
        QUARKUS_HTTP_PORT         = "4009"
        QUARKUS_HTTP_CORS         = "true"
        QUARKUS_HTTP_CORS_ORIGINS = "*"
      }

      template {
        # Discover short-url-mysql's dynamic host port through Nomad's own service
        # registry. The Consul-template equivalent ({{ range service ... }}) can't
        # be used: Nomad 2.0.4+ resolves a template's Consul query with a per-task
        # token derived through Consul workload identity, and no JWT auth method is
        # configured in our Consul, so the query returns 403 "ACL not found" and
        # the task is killed. nomadService uses the task's Nomad workload identity,
        # which works. Switch back to Consul once its workload identity is
        # configured -- see ../rabbitmq-cluster-plan.md and the cas job (cas-war
        # repo, deploy/nomad/cas.hcl), which still carry a hardcoded pin for the
        # same reason.
        #
        # A short-url-mysql reschedule re-renders this and restarts the task
        # (template change_mode defaults to "restart"): a brief blip on short-url,
        # which is acceptable.
        data = <<-EOF
          {{ range nomadService "short-url-mysql" -}}
          QUARKUS_DATASOURCE_JDBC_URL=jdbc:mysql://{{ .Address }}:{{ .Port }}/short_url?characterEncoding=UTF-8
          {{ end -}}
          QUARKUS_DATASOURCE_USERNAME={{ with secret "secret/data/short-url/mysql" }}{{ .Data.data.username }}{{ end }}
          QUARKUS_DATASOURCE_PASSWORD={{ with secret "secret/data/short-url/mysql" }}{{ .Data.data.password }}{{ end }}
        EOF
        destination = "secrets/app.env"
        env         = true
      }

      shutdown_delay = "5s"

      resources {
        cpu    = 200
        memory = 512
      }

      service {
        name = "short-url"
        port = "http"

        check {
          type     = "http"
          path     = "/q/health"
          interval = "10s"
          timeout  = "2s"
        }
      }
    }
  }
}

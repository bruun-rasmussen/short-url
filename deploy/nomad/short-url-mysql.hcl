job "short-url-mysql" {
  datacenters = ["BR1"]
  type        = "service"
  node_pool   = "production"

  group "short-url-mysql" {
    count = 1

    vault {
      role = "nomad-workloads"
    }

    network {
      port "mysql" { to = 3306 }
    }

    # Persistent storage on the NetApp share (netapp01:/thor, NFSv4.1), declared on
    # every thor in iac (`nomad_nfs_volumes` in ansible/group_vars/thor.yml), so no
    # node constraint: a drain or reschedule takes the data along. InnoDB's lock on
    # ibdata1 holds over NFSv4.1, so a second mysqld can't open the same datadir.
    # Until 2026-10-02 it was pinned to thor4's local disk.
    volume "short-url-mysql-data" {
      type   = "host"
      source = "short-url-mysql-data"
    }

    task "mysql" {
      driver = "docker"

      config {
        image = "mysql:8.4"
        ports = ["mysql"]
      }

      volume_mount {
        volume      = "short-url-mysql-data"
        destination = "/var/lib/mysql"
      }

      env {
        TZ             = "Europe/Copenhagen"
        MYSQL_DATABASE = "short_url"
      }

      template {
        data = <<-EOF
          MYSQL_ROOT_PASSWORD={{ with secret "secret/data/short-url/mysql" }}{{ .Data.data.root_password }}{{ end }}
          MYSQL_USER={{ with secret "secret/data/short-url/mysql" }}{{ .Data.data.username }}{{ end }}
          MYSQL_PASSWORD={{ with secret "secret/data/short-url/mysql" }}{{ .Data.data.password }}{{ end }}
        EOF
        destination = "secrets/mysql.env"
        env         = true
      }

      shutdown_delay = "5s"

      resources {
        cpu    = 300
        memory = 512
      }

      # Native Nomad registration (not Consul) so short-url can resolve this
      # task's dynamic host port with {{ nomadService "short-url-mysql" }} -- no
      # Consul-template query, which is broken under Nomad 2.0.4+ (see
      # jobs/short-url.hcl). Nothing else consumes this service.
      service {
        name     = "short-url-mysql"
        port     = "mysql"
        provider = "nomad"

        check {
          type     = "tcp"
          interval = "10s"
          timeout  = "2s"
        }
      }
    }
  }
}

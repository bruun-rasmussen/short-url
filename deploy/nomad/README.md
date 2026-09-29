# short-url – Nomad deployment

Job specs for running short-url on the BR1 Nomad cluster. Cluster-wide operations (Vault unsealing, Consul, scheduler behaviour) are documented in the `iac` repo under `nomad/README.md`.

| Job               | Count | Node    | Port           | Purpose                                          |
|-------------------|-------|---------|----------------|--------------------------------------------------|
| `short-url`       | 1     | any     | 4009 (static)  | The Quarkus app; DB credentials from Vault       |
| `short-url-mysql` | 1     | thor1   | dynamic        | MySQL 8.4 backing store                          |

Both run in node pool `production`, datacenter `BR1`.

## Deploying

```bash
nomad job validate short-url.hcl
nomad job plan short-url.hcl
nomad job run short-url.hcl
```

The image tag in `short-url.hcl` (`degas.bruun-rasmussen.dk:5000/osa/short-url:<version>`) must be bumped by hand after each release. `short-url` has no `update` stanza, so a deploy is an in-place replacement with a brief outage.

## How the pieces connect

**Database discovery.** `short-url-mysql` registers with `provider = "nomad"` and gets a dynamic host port. `short-url` finds it with `{{ range nomadService "short-url-mysql" }}` in its template and builds `QUARKUS_DATASOURCE_JDBC_URL` from the address and port. If `short-url-mysql` is rescheduled, the template re-renders and `short-url` restarts (default `change_mode`), which is a brief blip.

Do not switch this to a Consul `{{ range service ... }}` lookup. Since Nomad 2.0.4, template Consul queries need Consul workload identity, which isn't configured. The query returns 403 `ACL not found` and the task is killed. This caused the 2026-09-08 outage of `short-url` (and `cas`). Both jobs were first pinned to a hardcoded host:port, then moved to `nomadService`. The rule is to use `nomadService` for Nomad-to-Nomad discovery and `{{ with secret ... }}` for credentials.

**Secrets.** Both jobs carry `vault { role = "nomad-workloads" }`. Without it, `{{ with secret ... }}` renders blank instead of failing loudly. Credentials live in a single KV v2 path:

| Path                     | Fields                                  | Used by                    |
|--------------------------|-----------------------------------------|----------------------------|
| `secret/short-url/mysql` | `username`, `password`, `root_password` | short-url, short-url-mysql |

```bash
vault kv get secret/short-url/mysql
vault kv patch secret/short-url/mysql password="newvalue"   # patch merges; put would overwrite all fields
```

If a job won't start and template rendering fails, check `vault status` first: a sealed Vault is a more likely cause than a bad token.

**Health check.** `short-url` is checked at `/q/health` (SmallRye Health), which the Consul service registration uses. `short-url-mysql` has a plain TCP check.

**Storage.** MySQL data lives on the host volume `short-url-mysql-data`, declared on the Nomad client in the `iac` repo (`ansible/roles/nomad/templates/nomad-client.hcl`). It replaced a bind mount into `/var/lib/docker/volumes/...` that bypassed Docker's reference counting, so `docker volume prune` could have wiped it. Data survives allocation replacement. `short-url-mysql` is pinned to `thor1.bruun-rasmussen.dk` because the volume is local to that node.

**Shutdown.** Both jobs set `shutdown_delay = "5s"`. This gives the consul-template instances on the Live-Front hosts time to re-render the nginx upstream list before the process exits. Removing it causes connection errors during deploys.

## Gotchas

- `short-url` uses static port 4009, so it can only be placed on a node where 4009 is free.
- Deploy order matters only on first rollout: `short-url` won't render its template until `short-url-mysql` has a healthy registered instance.
- The comments in the `.hcl` files reference `jobs/short-url.hcl`, `../rabbitmq-cluster-plan.md` and the `cas` job from before the move. Those paths are stale.

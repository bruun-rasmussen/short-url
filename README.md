# Short-URL

A flexible URL shortening service built with Quarkus that generates short tags for URLs and provides QR code generation in PNG and SVG formats.

## Features

- **URL Shortening**: Create short, memorable tags for long URLs using customizable schemes
- **QR Code Generation**: Generate QR codes in PNG or SVG format for any shortened URL
- **Flexible Schemes**: Define multiple URL schemes with pattern matching and custom tag generation
- **RESTful API**: Simple HTTP endpoints for redirects, metadata, and QR code generation
- **Persistent Storage**: MySQL database with Flyway migrations for schema management

## API Endpoints

### Redirect to Target URL
```http
GET /{tag}
```
Returns a 303 redirect to the target URL associated with the tag.

**Get metadata instead of redirecting:**
```http
GET /{tag}?info=true
```
Returns JSON metadata about the shortened URL.

### Generate QR Code (PNG)
```http
GET /{scheme}/{target}-qr.png?size=256&margin=0&ecc=M
```
Generates a PNG QR code for the shortened URL.

**Query Parameters:**
- `size`: Image size in pixels (default: 256)
- `margin`: Margin size (default: 0)
- `ecc`: Error correction level - L/M/Q/H (default: M)

### Generate QR Code (SVG)
```http
GET /{scheme}/{target}-qr.svg?scale=4&margin=0&ecc=M
```
Generates an SVG QR code for the shortened URL.

**Query Parameters:**
- `scale`: Scale factor (default: 4)
- `margin`: Margin size (default: 0)
- `ecc`: Error correction level - L/M/Q/H (default: M)

## Architecture

The application uses two core models:

- **ShortScheme**: Defines URL shortening schemes with regex patterns, replacements, and tag generation rules
- **ShortUrlTag**: Maps tags to target URLs within a scheme, automatically generating unique tags as needed

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
mvn quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
mvn package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
mvn package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
mvn package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
mvn package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/short-url-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Testing

Run all tests:
```shell script
mvn test
```

Run a specific test class:
```shell script
mvn test -Dtest=ApplicationTest
```

Run a specific test method:
```shell script
mvn test -Dtest=ApplicationTest#getKnownShortcut
```

## Configuration

The application uses YAML configuration files in `src/main/resources/`:
- `application.yml`: Main configuration
- `application-dev.yml`: Development profile overrides
- `application-test.yml`: Test profile overrides

Database migrations are managed by Flyway and located in `src/main/resources/db/migration/`.

## Releasing a New Version

Releasing a new version is streamlined with the Maven Release Plugin:

```shell script
mvn release:prepare
```

This single command handles the entire release process:
1. Runs tests to ensure everything passes
2. Updates the version in `pom.xml` (removes `-SNAPSHOT`)
3. Creates a Git tag for the release
4. Builds and tags the Docker image with the release version
5. Pushes the Docker image to the registry (configured as `degas.bruun-rasmussen.dk:5000`)
6. Increments to the next SNAPSHOT version
7. Commits and pushes all changes to Git

**Note:** There is no separate `mvn release:perform` step required. The Docker image is automatically built, tagged, and pushed during the `:prepare` phase thanks to the Quarkus JIB container image plugin configuration.

## Technology Stack

- **Quarkus 3.18.3**: Java framework
- **Hibernate ORM with Panache**: Simplified persistence layer
- **MySQL 8.4**: Database
- **Flyway**: Database migrations
- **ZXing 3.4.1**: QR code generation
- **Jackson**: JSON serialization
- **Lombok**: Boilerplate reduction

## Related Guides

- JDBC Driver - MySQL ([guide](https://quarkus.io/guides/datasource)): Connect to the MySQL database via JDBC
- Hibernate Validator ([guide](https://quarkus.io/guides/validation)): Validate object properties (field, getter) and method parameters for your beans (REST, CDI, Jakarta Persistence)
- Flyway ([guide](https://quarkus.io/guides/flyway)): Handle your database schema migrations
- YAML Configuration ([guide](https://quarkus.io/guides/config-yaml)): Use YAML to configure your Quarkus application

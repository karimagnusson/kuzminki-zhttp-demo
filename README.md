| Discord | Twitter |
| --- | --- |
| [![badge-discord](https://img.shields.io/discord/629491597070827530?logo=discord)](https://discord.com/channels/629491597070827530/1063139826636963931) | [![Twitter Follow](https://img.shields.io/twitter/follow/kuzminki_lib?label=follow&style=flat&logo=twitter&color=brightgreen)](https://twitter.com/kuzminki_lib) |

# kuzminki-zhttp-demo

kuzminki-zhttp-demo is an example REST API using [kuzminki-zio-2](https://github.com/karimagnusson/kuzminki-zio-2) and [zio-http](https://github.com/dream11/zio-http).

This demo uses ZIO 2, zio-http 3.0.0-RC1 and kuzminki-zio-2 0.9.5-RC4.

Examples:
- Select, insert, update, delete
- Cached queries
- Streaming
- Jsonb field
- Array field
- Timestamp methods

#### Database

```sql
CREATE DATABASE world;
```

```bash
psql world < db/world.pg
```

#### Config

```sbt
// src/main/resources/application.conf

db {
  name = "world"
  user = "<USER>"
  pwd = "<PASS>"
}
```

#### Postman

If you use [Postman](https://www.postman.com/) you can import `postman/demo.json` where all the endpoints are set up.

#### Run

```sbt
sbt run
```
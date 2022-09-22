# kuzminki-zhttp-demo

kuzminki-zhttp-demo is an example REST API using [kuzminki-zio-2](https://github.com/karimagnusson/kuzminki-zio-2) and [zio-http](https://github.com/dream11/zio-http).

This demo uses ZIO 2 and zhttp-2.0.0-RC11. It adds examples for jsonb field, array field and streaming.

#### Database

```sql
CREATE DATABASE world;
```

```bash
psql world < db/world.pg
```

#### Postman

If you use [Postman](https://www.postman.com/) you can import `postman/demo.json` where all the endpoints are set up.

#### Run

```sbt
sbt run
```
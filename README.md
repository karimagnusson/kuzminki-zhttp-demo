# kuzminki-zhttp-demo

kuzminki-zhttp-demo is an example of a REST API using [kuzminki-zio](https://github.com/karimagnusson/kuzminki-zio) and [zio-http](https://github.com/dream11/zio-http).

#### Database

```sql
CREATE DATABASE world;
```

```bash
psql world < db/world.pg
```

#### Postman

If you use [Postman](https://www.postman.com/) you can import `postman/play-demo.json` where all the endpoints are set up.

#### Run

```sbt
sbt run
```
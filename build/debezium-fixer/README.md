## Debezium Fixer

Some of the debeium connections to postgres are sometimes failing, after running(succesfully) for a while, throwing a "PSQLException".
To overcome that problem we running this docker image container as a secondary container on the debezium pods and checking if any of these tasks failed. Then restarting that one particular task.

How to build:
```sh
docker build .
```

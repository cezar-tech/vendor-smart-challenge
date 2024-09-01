# vendor-smart-challenge

## Docker Build And Run

```
docker build -t vs-challenge .
docker run -p 8080:8080 vs-challenge
```

A Swagger endpoint is exposed at http://localhost:8080/swagger-ui/index.html

## Document what you would improve

* Perform load testing specially for the search endpoint;
* Introduce property based testing to the unit tests;
* Separate @Controller and @Repository by entities managed, and for the search implement it in a
  @Service stereotype;
* Implement e2e testing for cloud environments.
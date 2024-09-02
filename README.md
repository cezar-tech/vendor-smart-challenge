# vendor-smart-challenge

## Docker Build And Run

```
docker build -t vs-challenge .
docker run -p 8080:8080 vs-challenge
```

A Swagger endpoint is exposed at http://localhost:8080/swagger-ui/index.html

## Document what you would improve

* Need to solve CSRF blocking requests for spring, but I didn't have time for it;
* Perform load testing specially for the search endpoint;
* Improve unit tests and controller contract tests for data format, invalid request and coverage in
  general;
* Introduce property based testing to the unit tests;
* Separate @Controller and @Repository by entities managed, and for the search implement it in a
  @Service stereotype;
* Implement e2e testing for cloud environments;
* Move password and username to secrets database (like [Vault](https://www.vaultproject.io/)) or any
  cloud secrets service, though a database might be better for managing it.
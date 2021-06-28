# spring-argument-resolver-demo

Run it with:
```
./mvnw spring-boot:run
```

Example request:
```
curl -X POST -H "header1: myValue" -H "Content-Type: application/json" --data "{\"bodyValue1\": \"myBodyValue\"}" http://localhost:8080/
```

expected output:
```
Request headers: myValue, null, Request body values: myBodyValue, null
```

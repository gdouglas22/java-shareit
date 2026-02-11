# java-shareit

Multi-module project:

- `server` (`shareit-server`) - business logic, DB access, runs on `9090`
- `gateway` (`shareit-gateway`) - input validation and proxy layer, runs on `8080`

Build all modules from root:

```bash
mvn clean install
```

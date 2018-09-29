# esm-http-listener
> Deploy with grizzly-http to listen esm shell commond, then make it call in one thread

## QuickStart
### start server
1. clone && cd esm-http-listener
2. mvn package
3. save grizzly-http.zip into `dest_dir`
4. cd `dest_dir`
5. unzip grizzly-http.zip
6. sh script/run.sh `your_ip` `your_port`

### check status
1. sh script/check.sh

### stop server
1. sh script/stop.sh

## Refenerce
- [scala shell](https://www.scala-lang.org/api/current/scala/sys/process/ProcessBuilder.html)
- [esm](https://github.com/medcl/esm)

## REMOVE COMMIT
`git push -f origin Head^^^:master`
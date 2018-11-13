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

## REMOVE COMMIT
`git push -f origin Head^^^:master`


## Benchmark
|src_cluster|dest_cluster|src_index_size|src_index_doc|dest_index_size|dest_index_doc|Elapsed time in second|
|---|---|---|---|---|---|---|
|1|2|3|4|5|6|7|
|46 nodes 1,008 indices 7,883 shards 384,830,680,462 docs 100.78TB|2 nodes 32 indices 209 shards 131,012,723 docs 5.08GB|648311|144.13mb|647,774|149.5mb|55|
|~|~|11,205,805|1.5gb|11,191,609|2.13gb|466|
|~|~|847,684,467|109.56gb|xx|xx|Timeout: timeout:worried:|

P.S.,
- the difference between src and dest may caused by index is not static, but dynameic changing.
- when the session abort or timeout, the task still in background

## Reference
- [scala shell](https://www.scala-lang.org/api/current/scala/sys/process/ProcessBuilder.html)
- [esm](https://github.com/medcl/esm)

ps -ef | grep gemfire | grep -v grep | awk '{print $2}' | xargs kill -9

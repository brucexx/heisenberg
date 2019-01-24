Q:不要分库表的怎么搞?<br>

A:只需要在schema里加上data_node即可，如下<br>


\<schema name="test"  ***dataNode="local_node"*** /\><br/>
\<table name="test" dataNode="local_node" shardName="t" rule="rule2"/\> <br/>
\</schema\><br>

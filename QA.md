Q:不要分库表的怎么搞?<br>

A:只需要在schema里属性里加上data_node即可，如下<br>

```xml
<schema name="test" dataNode="local_node">
		<table name="test" dataNode="local_node1$0-2" shardName="t" rule="rule1"/>
	</schema>
 

	<dataNode name="local_node">
		<property name="dataSource">
			<dataSourceRef>localDB$0</dataSourceRef>
			<!-- slave,暂无 -->
			<!-- dataSourceRef>ds_shard_slave$0-3</dataSourceRef -->
		</property>
		<property name="poolSize">5</property>
		<property name="coreSize">5</property>
		<property name="heartbeatSQL">select user()</property>
	</dataNode>
	
	<dataNode name="local_node1">
		<property name="dataSource">
			<dataSourceRef>localDB$0-2</dataSourceRef>
			<!-- slave,暂无 -->
			<!-- dataSourceRef>ds_shard_slave$0-3</dataSourceRef -->
		</property>
		<property name="poolSize">5</property>
		<property name="coreSize">5</property>
		<property name="heartbeatSQL">select user()</property>
	</dataNode>


	<dataSource name="localDB" type="mysql">
		<property name="location">
			<location>127.0.0.1:3306/test$1-3</location>
		</property>
		<property name="user">root</property>
		<property name="password"></property>
	</dataSource>
```


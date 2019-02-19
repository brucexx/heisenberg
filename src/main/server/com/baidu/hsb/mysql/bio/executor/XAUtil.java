/**
 * Copyright (C) 2019 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.hsb.mysql.bio.executor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;

import com.baidu.hsb.HeisenbergConfig;
import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.mysql.MySQLDataNode;
import com.baidu.hsb.route.RouteResultsetNode;
import com.baidu.hsb.route.util.StringUtil;

/**
 * @author brucexx
 *
 */
public class XAUtil {
	private static Set<Integer> s = new HashSet<Integer>();

	static {
		s.add(1399);
		s.add(1440);
	}

	public static RouteResultsetNode[] genNodesByAddr(RouteResultsetNode[] nodes) {
		HeisenbergConfig conf = HeisenbergServer.getInstance().getConfig();
		Map<String, List<RouteResultsetNode>> map = new TreeMap<String, List<RouteResultsetNode>>();
		for (RouteResultsetNode n : nodes) {
			final MySQLDataNode dn = conf.getDataNodes().get(n.getName());
			if (dn == null) {
				throw new RuntimeException("no datanode found! ");
			}
			String addr = dn.getSource().getConfig().getHost() + ":" + dn.getSource().getConfig().getPort();
			if (map.get(addr) == null) {
				map.put(addr, new ArrayList<RouteResultsetNode>());
			}
			map.get(addr).add(n);
		}

		RouteResultsetNode[] rnArr = new RouteResultsetNode[map.size()];
		int i = 0;
		for (List<RouteResultsetNode> list : map.values()) {
			rnArr[i++] = mergeRn(list);
		}
		return rnArr;
	}

	private static RouteResultsetNode mergeRn(List<RouteResultsetNode> nodes) {
		// 这里过滤掉重复的
		Set<String> sql = new LinkedHashSet<String>();
		List<String> name = new ArrayList<String>();
		for (RouteResultsetNode rn : nodes) {
			sql.addAll(Arrays.asList(rn.getStatement()));
			name.add(rn.getName());
		}

		return new RouteResultsetNode(StringUtil.join(name, "#"), sql.toArray(new String[sql.size()]));

	}

	public static boolean isXAIgnore(int errno) {
		return s.contains(errno);
	}

	public static boolean isXaStart(String sql) {
		String s = StringUtil.lowerCase(sql);
		return StringUtil.contains(s, "xa") && StringUtil.contains(s, "start");
	}

	public static boolean isXAOp(String sql) {
		String s = StringUtil.lowerCase(sql);
		return StringUtil.contains(s, "xa");
	}

	public static String convert(int op) {
		switch (op) {
		case 0:
			return "start";
		case 1:
			return "end&prepare";
		case 2:
			return "commit";
		case 3:
			return "rollback";
		}
		return "";

	}

	public static String[] gen(int op, String xId, String oSql) {
		switch (op) {
		case 0:
			return new String[] { "xa start '" + xId + "' ", oSql };
		case 1:
			return new String[] { "xa end '" + xId + "' ", "xa prepare '" + xId + "' " };
		case 2:
			return new String[] { "xa commit '" + xId + "' " };
		case 3:
			return new String[] { "xa end '" + xId + "' ", "xa rollback '" + xId + "' " };
		}
		return new String[] {};
	}

	public static String[] genMulti(int op, String xId, RouteResultsetNode[] nodes) {
		switch (op) {
		case 0:
			rebuild(nodes, xId);
			break;
		case 1:
			rego(nodes, xId, new String[] { "xa end '" + xId + "' ", "xa prepare '" + xId + "' " });
			break;
		case 2:
			rego(nodes, xId, new String[] { "xa commit '" + xId + "' " });
			break;
		case 3:
			rego(nodes, xId, new String[] { "xa end '" + xId + "' ", "xa rollback '" + xId + "' " });
			break;
		}
		return new String[] {};
	}

	private static void rebuild(RouteResultsetNode[] nodes, String xId) {
		for (RouteResultsetNode rn : nodes) {
			rn.setStatement(
					(String[]) ArrayUtils.addAll(new String[] { "xa start '" + xId + "' " }, rn.getStatement()));
		}
	}

	private static void rego(RouteResultsetNode[] nodes, String xId, String[] stmt) {
		for (RouteResultsetNode rn : nodes) {
			rn.setStatement(stmt);
		}
	}

}

package cn.livefree.utils;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;

public class DBUtil {
	private static BasicDataSource dataSource;
	static ThreadLocal<Connection> tl;

	static {
		try {
			tl = new ThreadLocal<Connection>();
			Properties prop = new Properties();
			prop.load(new FileInputStream("config.properties"));
			String driverName = prop.getProperty("driver");
			String host = prop.getProperty("host");
			String username = prop.getProperty("username");
			String password = prop.getProperty("password");
			int maxActive = Integer.parseInt(prop.getProperty("maxactive"));
			int maxWait = Integer.parseInt(prop.getProperty("maxwait"));

			dataSource = new BasicDataSource();
			dataSource.setDriverClassName(driverName);
			dataSource.setUrl(host);
			dataSource.setUsername(username);
			dataSource.setPassword(password);

			dataSource.setMaxActive(maxActive);
			dataSource.setMaxWait(maxWait);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static Connection getConnection() throws ClassNotFoundException, SQLException {
		Connection conn = dataSource.getConnection();
		tl.set(conn);
		return conn;
	}

	public static void closeConnection() {
		try {
			Connection conn = tl.get();
			if (conn != null) {
				conn.close();
			}
			tl.remove();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ResultSet executeQuery(String sql) {
		Connection conn = tl.get();
		try {
			Statement stmt = conn.createStatement();
			return stmt.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int executeUpdate(String sql) {
		int result = 0;
		Connection conn = tl.get();
		try {
			Statement stmt = conn.createStatement();
			result = stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static Object execute(String sql) {
		boolean b = false;
		Connection conn = tl.get();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			b = stmt.execute(sql);
			if (b) {
				return stmt.getResultSet();
			} else {
				return stmt.getUpdateCount();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static ResultSet executeQuery(String sql, Object[] in) {
		Connection conn = tl.get();
		PreparedStatement pst = null;
		try {
			pst = conn.prepareStatement(sql);
			for (int i = 0; i < in.length; i++)
				pst.setObject(i + 1, in[i]);
			return pst.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static PreparedStatement pstmt(String sql) {
		Connection conn = tl.get();
		try {
			return conn.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object execute(String sql, Object[] in) {
		boolean b = false;
		try {
			Connection conn = tl.get();
			PreparedStatement pst = conn.prepareStatement(sql);
			for (int i = 0; i < in.length; i++)
				pst.setObject(i + 1, in[i]);
			b = pst.execute();
			if (b) {
				return pst.getResultSet();
			} else {
				List<Integer> list = new ArrayList<Integer>();
				list.add(pst.getUpdateCount());
				while (pst.getMoreResults()) {
					list.add(pst.getUpdateCount());
				}
				return list;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	// 时间转换
	public static Date date(String date_str) {
		try {
			Calendar cal = Calendar.getInstance();// 日期类
			Timestamp timestampnow = new Timestamp(cal.getTimeInMillis());// 转换成正常的日期格式
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");// 改为需要的东西
			ParsePosition pos = new ParsePosition(0);
			java.util.Date current = formatter.parse(date_str, pos);
			timestampnow = new Timestamp(current.getTime());
			return timestampnow;
		} catch (NullPointerException e) {
			return null;
		}
	};

}

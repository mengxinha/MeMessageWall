import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.sun.org.apache.xml.internal.security.algorithms.MessageDigestAlgorithm;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@WebServlet("/message")
public class MessageServlet extends HttpServlet {
    //这个对象在多个方法中都要使用
   private final ObjectMapper objectMapper = new ObjectMapper();

   //创建一个List，使用List来保存message
   /*
   private List<Message> messageList = new ArrayList<>();
   */


    //让页面获取到数据 把 messageList 中的内容返回到页面上
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //显式声明当前的响应数据格式 不要让客户端去猜
        resp.setContentType("application/json;charset=utf8");
        //把messageList转成json字符串，并且返回给页面就行了
        /*
        resp.getWriter().write(objectMapper.writeValueAsString(messageList));
         */
        try {
            resp.getWriter().write(objectMapper.writeValueAsString(load()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //提交数据 ： 发送一个post请求    (这可以看成一个服务器吗？服务器接受message，并把接收到的message保存到内存中
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       //获取body中的数据并解析
        Message message = objectMapper.readValue(req.getInputStream(),Message.class);
       //把 message 保存一下，简单的办法就是保存到内存中
        //把收到的 message 保存到 messageList中
        /*
        messageList.add(message);
         */
        try {
            save(message);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        resp.setStatus(200);

        System.out.println("提交数据成功" +"from = " + message.getFrom()+
                ",   to= "+ message.getTo()+"   ,message= " +message.getMessage());
        //后端代码完成

    }

    //以下是使用数据库所写的代码
    private List<Message> load() throws SQLException {
        //从数据库查询数据

        //1,现有一个数据源
        DataSource dataSource = new MysqlDataSource();
        ((MysqlDataSource)dataSource).setUrl("jdbc:mysql://127.0.0.1:3306/java105?characterEncoding=utf8&useSSL=false");
        ((MysqlDataSource)dataSource).setUser("root");
        ((MysqlDataSource)dataSource).setPassword("mysql");

        //2,建立连接
        Connection connection = (Connection) dataSource.getConnection();

        //3,构造SQL语句
        String sql = "select * from message";
        PreparedStatement statement = connection.prepareStatement(sql);


        //4,执行SQL
        ResultSet resultSet = statement.executeQuery();

        //5,遍历结果集合
        List<Message> messageList = new ArrayList<>();
        while (resultSet.next()){
            Message message = new Message();
            message.setFrom(resultSet.getString("from"));
            message.setTo(resultSet.getString("to"));
            message.setMessage(resultSet.getString("message"));
            messageList.add(message);
        }

        //6,关闭连接
        statement.close();
        connection.close();
        return messageList;


    }
    private void save(Message message) throws SQLException {
        //把数据保存到数据库中

        //1,现有一个数据源
        DataSource dataSource = new MysqlDataSource();
        ((MysqlDataSource)dataSource).setUrl("jdbc:mysql://127.0.0.1:3306/java105?characterEncoding=utf8&useSSL=false");
        ((MysqlDataSource)dataSource).setUser("root");
        ((MysqlDataSource)dataSource).setPassword("mysql");

        //2,建立连接
        Connection connection = (Connection) dataSource.getConnection();

        //3,构造SQL语句
        String sql = "insert into message value(?,?,?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1,message.getFrom());
        statement.setString(2,message.getTo());
        statement.setString(3,message.getMessage());

        //4,执行SQL
        int ret = statement.executeUpdate();
        System.out.println("ret ="+ret);

        //5,关闭连接
        statement.close();
        connection.close();
    }
}

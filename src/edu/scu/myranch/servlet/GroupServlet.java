package edu.scu.myranch.servlet;

import com.alibaba.fastjson.JSON;
import edu.scu.myranch.utils.DBUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.*;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

@WebServlet({"/group/addGroup", "/group/delGroup", "/group/addBatch", "/group/delBatch", "/group/getFiles",
        "/group/getUserName", "/group/cd","/group/showProduction", "/group/addProduction", "/group/delProduction",
        "/group/changeProduction"})
public class GroupServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if ("/group/addGroup".equals(servletPath)) {
            doAddGroup(request, response);
        } else if ("/group/delGroup".equals(servletPath)) {
            doDelGroup(request, response);
        } else if ("/group/addBatch".equals(servletPath)) {
            try {
                doAddBatch(request, response);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if ("/group/delBatch".equals(servletPath)) {
            try {
                doDelBatch(request, response);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if ("/group/getFiles".equals(servletPath)) {
            doGetFiles(request, response);
        } else if ("/group/getUserName".equals(servletPath)) {
            doGetUserName(request, response);
        } else if ("/group/cd".equals(servletPath)) {
            doCd(request, response);
        } else if ("/group/showProduction".equals(servletPath)){
            try {
                doShowProduction(request,response);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if ("/group/addProduction".equals(servletPath)) {
            try {
                doAddProduction(request,response);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if ("/group/delProduction".equals(servletPath)) {
            try {
                doDelProduction(request,response);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if ("/group/changeProduction".equals(servletPath)) {
            try {
                doChangeProduction(request,response);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void doCd(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null) {
            String curDir = (String) session.getAttribute("curDir");
            String filename = request.getParameter("filename");
            if ("..".equals(filename)){
                String rootDir = (String) session.getAttribute("rootDir");
                if (!curDir.equals(rootDir)) {
                    String newDir = curDir.substring(0, curDir.lastIndexOf('/'));
                    File file = new File(newDir);
                    if (file.exists() && file.isDirectory()) {
                        session.setAttribute("curDir", newDir);
                        out.print("0");
                    } else {
                        out.print("1");
                    }
                }
            } else {
                String newDir = curDir + "/" + filename;
                File file = new File(newDir);
                if (file.exists() && file.isDirectory()) {
                    session.setAttribute("curDir", newDir);
                    out.print("0");
                } else {
                    out.print("1");
                }
            }
        }
    }

    private void doGetUserName(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null) {
            String username = (String) session.getAttribute("username");
            out.print(username);
        }
    }

    private void doAddGroup(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null) {
            String curDir = (String) session.getAttribute("curDir");
            String groupName = request.getParameter("groupName");
            File file = new File(curDir + "/" + groupName);
            if (!file.exists() && file.mkdir()) {
//                等于0代表新建分组成功，等于1代表新建分组失败
                out.print("0");
            } else {
                out.print("1");
            }
        }
    }

    private void doDelGroup(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null) {
            String curDir = (String) session.getAttribute("curDir");
            String groupName = request.getParameter("groupName");
            File file = new File(curDir + "/" + groupName);
            if (deleteDir(file)) {
                out.print("0");
            } else {
                out.print("1");
            }
        }
    }


    private static boolean deleteDir(File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            return false;
        }

        // 递归删除非空目录
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                deleteDir(f);
            } else {
                f.delete();
            }
        }

        return dir.delete();
    }

    private void doAddBatch(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null) {
            String curDir = (String) session.getAttribute("curDir");
            String batchName = request.getParameter("batchName");

//            System.out.println(batchName);
//            System.out.println(curDir);

            File f = new File(curDir + "/" + batchName + ".txt");

            if(!f.exists() && batchName != "") {
                String batchNum = Long.toHexString(new Random().nextLong(0x1000000000000L - 0x100000000000L) + 0x100000000000L);

//                System.out.println(batchNum);

                Connection conn = DBUtils.getConnection();
//                String sql = "select * from ?";
                String sql = " SELECT table_name FROM information_schema.TABLES WHERE table_name = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1,batchNum);
                ResultSet rs = ps.executeQuery();

//                如果表已经存在，重新生成一个随机数表名字
                while (rs.next()){
                    batchNum = Long.toHexString(new Random().nextLong(0x1000000000000L - 0x100000000000L) + 0x100000000000L);
                    sql = "SELECT table_name FROM information_schema.TABLES WHERE table_name = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1,batchNum);
                    rs = ps.executeQuery();
                }

//              跳出循环，表名不存在，可以创建

                sql = "CREATE TABLE IF NOT EXISTS " + batchNum + "(\n" +
                        "`id` int(10) NOT NULL AUTO_INCREMENT COMMENT '产品编号',\n" +
                        "`food` varchar(30) NOT NULL DEFAULT '未知' COMMENT '肥料/饲料',\n" +
                        "`growDuration` varchar(20) NOT NULL DEFAULT '未知' COMMENT '成长周期',\n" +
                        "`healthy` varchar(20) NOT NULL DEFAULT '健康' COMMENT '健康状况',\n" +
                        "`description` varchar(50) DEFAULT NULL COMMENT '备注',\n" +
                        "PRIMARY KEY (`id`)\n" +
                        ") ENGINE=InnoDB CHARSET=utf8";

                Statement st = conn.createStatement();
                int i = st.executeUpdate(sql);


//                System.out.println(sql);

//                成功就创建文件
                if(i == 0){
                    f.createNewFile();
                    byte[] bytes = new byte[1024];
                    bytes = batchNum.getBytes();
                    int b = bytes.length;
                    OutputStream os = new FileOutputStream(f);
                    os.write(bytes,0,b);
//                    os.write(bytes);
                    os.close();
                    DBUtils.close(conn,null,rs);
                    if(st != null){
                        st.close();
                    }
                    out.print("0");
                }
            }
            else{
                out.print("1");
            }
        }
    }

    private void doDelBatch(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null) {
            String curDir = (String) session.getAttribute("curDir");
            String batchName = request.getParameter("batchName");
            String fileName = curDir + "/" + batchName;

//            System.out.println(curDir);
//            System.out.println(batchName);
//            System.out.println(fileName);

            File file = new File(fileName);
            if (file.exists() && file.isFile()) {

                String batchNum = getBatchNum(fileName);

                if(file.delete()){
                    Connection conn = DBUtils.getConnection();
                    String sql = "DROP TABLE IF EXISTS " + batchNum;
                    Statement st = conn.createStatement();
                    int i = st.executeUpdate(sql);
                    if(i == 0){
                        out.print("0");
                        DBUtils.close(conn,null,null);
                        if(st != null){
                            st.close();
                        }
                    }else {
                        out.print("1");
                    }
                }else {
                    out.print("1");
                }
            } else {
                out.print("1");
                }
        }
    }

    private void doGetFiles(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null) {
            String curDir = (String) session.getAttribute("curDir");
            File file = new File(curDir);
            File[] files = file.listFiles();
            ArrayList<JSONFile> fileList = new ArrayList<>();
            for (File f : files) {
                fileList.add(new JSONFile(f.getName(), f.isFile() ? 1 : 0));
            }
            out.print(JSON.toJSONString(fileList));
        }
    }

    private void doGoBackToParentDir(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");


    }

    private void doShowProduction(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null){
            String fileName = request.getParameter("filename");
            String curDir = (String) session.getAttribute("curDir");
            fileName = curDir + "/" + fileName;

            String batchNum = getBatchNum(fileName);

            Connection conn = DBUtils.getConnection();
            String sql = " SELECT * FROM " + batchNum;
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            ArrayList<batchProduction> batchProductions = new ArrayList<>();
            while(rs.next()){
                batchProduction batchProduction = new batchProduction();
                batchProduction.setProductionId(rs.getInt("id"));
                batchProduction.setFood(rs.getString("food"));
                batchProduction.setGrowDuration(rs.getString("growDuration"));
                batchProduction.setHealthy(rs.getString("healthy"));
                batchProduction.setDescription(rs.getString("description"));
                batchProductions.add(batchProduction);
            }

            String  jsonStr = JSON.toJSONString(batchProductions);

//            System.out.println(jsonStr);

            out.print(jsonStr);

            DBUtils.close(conn,ps,rs);

            session.setAttribute("curFile", fileName);
        }
    }

    private void doAddProduction(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null){
            String addNum = request.getParameter("addNum");
            String addFood = request.getParameter("addFood");
            String addHealthy = request.getParameter("addHealthy");
            String addGrowDuration = request.getParameter("addGrowDuration");
            String addDescription = request.getParameter("addDescription");
            String curFile = (String) session.getAttribute("curFile");
            String batchNum = getBatchNum(curFile);

            int flag = 0;

            Connection conn = DBUtils.getConnection();
            String sql = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            int productionNum = Integer.parseInt(addNum);
            for(int i = 0; i < productionNum; ++i){
                sql = "INSERT INTO " + batchNum + "(`food`,`growDuration`,`healthy`,`description`) VALUES " + "("
                        + "'" + addFood + "'" + "," + "'" + addGrowDuration + "'" + "," + "'" + addHealthy + "'" + "," + "'" + addDescription + "'" + ")";
                ps = conn.prepareStatement(sql);
                flag = ps.executeUpdate();
            }

            if (flag > 0) {
                sql =  " SELECT * FROM " + batchNum;
                ps = conn.prepareStatement(sql);
                rs = ps.executeQuery();
                ArrayList<batchProduction> batchProductions = new ArrayList<>();
                while(rs.next()){
                    batchProduction batchProduction = new batchProduction();
                    batchProduction.setProductionId(rs.getInt("id"));
                    batchProduction.setFood(rs.getString("food"));
                    batchProduction.setGrowDuration(rs.getString("growDuration"));
                    batchProduction.setHealthy(rs.getString("healthy"));
                    batchProduction.setDescription(rs.getString("description"));
                    batchProductions.add(batchProduction);
                }
                String jsonStr = JSON.toJSONString(batchProductions);
                out.print(jsonStr);
                DBUtils.close(conn,ps,rs);
            }
        }
    }

    private String getBatchNum(String fileName) {
        FileInputStream fis = null;
        byte[] buffer = new byte[12];
        StringBuilder sb = new StringBuilder();
        try {
            fis = new FileInputStream(fileName);

            while (fis.read(buffer) != -1) {
                sb.append(new String(buffer));
                buffer = new byte[12];
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        String batchNum = sb.toString();
        return batchNum;
    }

    private void doDelProduction(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null){
            String delId = request.getParameter("delId");
            String curFile = (String) session.getAttribute("curFile");
            String batchNum = getBatchNum(curFile);

            int delID = Integer.parseInt(delId);

            System.out.println(delId);
            System.out.println(curFile);
            System.out.println(batchNum);

            int flag = 0;

            Connection conn = DBUtils.getConnection();
            String sql = "DELETE FROM " + batchNum + " WHERE `id` = " + delID;
            PreparedStatement ps = conn.prepareStatement(sql);
            flag = ps.executeUpdate();
            ResultSet rs = null;

            if (flag > 0){
                sql = "SELECT * FROM " + batchNum;
                ps = conn.prepareStatement(sql);
                rs = ps.executeQuery();

                ArrayList<batchProduction> batchProductions = new ArrayList<>();
                while(rs.next()){
                    batchProduction batchProduction = new batchProduction();
                    batchProduction.setProductionId(rs.getInt("id"));
                    batchProduction.setFood(rs.getString("food"));
                    batchProduction.setGrowDuration(rs.getString("growDuration"));
                    batchProduction.setHealthy(rs.getString("healthy"));
                    batchProduction.setDescription(rs.getString("description"));
                    batchProductions.add(batchProduction);
                }
                String jsonStr = JSON.toJSONString(batchProductions);
                out.print(jsonStr);
                DBUtils.close(conn,ps,rs);
            }
        }
    }

    private void doChangeProduction(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession(false);
        if (session != null) {
            String cId = request.getParameter("cId");
            String cFood = request.getParameter("cFood");
            String cGrowDuration = request.getParameter("cGrowDuration");
            String cHealthy = request.getParameter("cHealthy");
            String cDescription = request.getParameter("cDescription");
            String curFile = (String) session.getAttribute("curFile");
            String batchNum = getBatchNum(curFile);

            Connection conn = DBUtils.getConnection();
            String sql = "UPDATE " + batchNum + " SET `food` = " + "'" + cFood + "'" + ",`growDuration` = " + "'" + cGrowDuration +
                    "'" + ",`healthy` = " + "'" + cHealthy + "'" + ",`description` = " + "'" + cDescription + "'" + " WHERE " + "`id` = " + Integer.parseInt(cId);
            System.out.println(sql);
            PreparedStatement ps = conn.prepareStatement(sql);
            int i = ps.executeUpdate();
            if (i > 0) {
                DBUtils.close(conn,ps,null);
                out.print("0");
            } else {
                DBUtils.close(conn,ps,null);
                out.print("1");
            }
//            UPDATE grade SET gradename = '高中' WHERE gradeid = 1;
//            UPDATE '8bdca76f09f9' SET `food` = '新的',`growDuration` = '新的修改',`healthy` = '刚刚',`description` = '刚刚' WHERE `id` = 61

        }
    }
}

class JSONFile {
    private String filename;
    private int type;

    public JSONFile(String filename, int type) {
        this.filename = filename;
        this.type = type;
    }

    public JSONFile() {
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JSONFile jsonFile = (JSONFile) o;
        return type == jsonFile.type && Objects.equals(filename, jsonFile.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, type);
    }

    @Override
    public String toString() {
        return "JSONFile{" +
                "filename='" + filename + '\'' +
                ", type=" + type +
                '}';
    }
}

class batchProduction{

    private int productionId;
    private String food;
    private String growDuration;
    private String healthy;
    private String description;

    public batchProduction(){

    }

    public batchProduction(int productionId, String food, String growDuration, String healthy, String description) {
        this.productionId = productionId;
        this.food = food;
        this.growDuration = growDuration;
        this.healthy = healthy;
        this.description = description;
    }

    public int getProductionId() {
        return productionId;
    }

    public void setProductionId(int productionId) {
        this.productionId = productionId;
    }

    public String getFood() {
        return food;
    }

    public void setFood(String food) {
        this.food = food;
    }

    public String getGrowDuration() {
        return growDuration;
    }

    public void setGrowDuration(String growDuration) {
        this.growDuration = growDuration;
    }

    public String getHealthy() {
        return healthy;
    }

    public void setHealthy(String healthy) {
        this.healthy = healthy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        batchProduction that = (batchProduction) o;
        return productionId == that.productionId && Objects.equals(food, that.food) && Objects.equals(growDuration, that.growDuration) && Objects.equals(healthy, that.healthy) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productionId, food, growDuration, healthy, description);
    }
}

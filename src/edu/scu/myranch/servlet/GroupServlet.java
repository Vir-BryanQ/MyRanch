package edu.scu.myranch.servlet;

import com.alibaba.fastjson.JSON;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;

@WebServlet({"/group/addGroup", "/group/delGroup", "/group/addBatch", "/group/delBatch", "/group/getFiles",
        "/group/getUserName", "/group/cd"})
public class GroupServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if ("/group/addGroup".equals(servletPath)) {
            doAddGroup(request, response);
        } else if ("/group/delGroup".equals(servletPath)) {
            doDelGroup(request, response);
        } else if ("/group/addBatch".equals(servletPath)) {
            doAddBatch(request, response);
        } else if ("/group/delBatch".equals(servletPath)) {
            doDelBatch(request, response);
        } else if ("/group/getFiles".equals(servletPath)) {
            doGetFiles(request, response);
        } else if ("/group/getUserName".equals(servletPath)) {
            doGetUserName(request, response);
        } else if ("/group/cd".equals(servletPath)) {
            doCd(request, response);
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

    private void doAddBatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null) {
            String curDir = (String) session.getAttribute("curDir");
            String batchName = request.getParameter("batchName");
        }
    }

    private void doDelBatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null) {
            String curDir = (String) session.getAttribute("curDir");
            String batchName = request.getParameter("batchName");
            File file = new File(curDir + "/" + batchName);
            if (file.exists() && file.isFile() && file.delete()) {
                out.print("0");
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


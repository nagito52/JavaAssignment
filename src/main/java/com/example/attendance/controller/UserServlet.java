package com.example.attendance.controller;

import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.example.attendance.dao.UserDAO;
import com.example.attendance.dto.User;

@WebServlet("/users")
public class UserServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        HttpSession session = req.getSession(false);
        User currentUser = (User) (session != null ? session.getAttribute("user") : null);

        // 管理者以外はログインページへ
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            resp.sendRedirect("login.jsp");
            return;
        }

        // 成功メッセージをセッションから取得してリクエストに渡す
        String message = (String) session.getAttribute("successMessage");
        if (message != null) {
            req.setAttribute("successMessage", message);
            session.removeAttribute("successMessage");
        }

        if ("list".equals(action) || action == null) {
            Collection<User> users = userDAO.getAllusers();
            req.setAttribute("users", users);
            RequestDispatcher rd = req.getRequestDispatcher("/jsp/user_management.jsp");
            rd.forward(req, resp);

        } else if ("edit".equals(action)) {
            String username = req.getParameter("username");
            User user = userDAO.findByUsername(username);
            req.setAttribute("userToEdit", user);

            Collection<User> users = userDAO.getAllusers();
            req.setAttribute("users", users);

            RequestDispatcher rd = req.getRequestDispatcher("/jsp/user_management.jsp");
            rd.forward(req, resp);

        } else {
            resp.sendRedirect("users?action=list");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        HttpSession session = req.getSession(false);
        User currentUser = (User) (session != null ? session.getAttribute("user") : null);

        // 管理者以外はログインページへ
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            resp.sendRedirect("login.jsp");
            return;
        }

        if ("add".equals(action)) {
            // ユーザー追加
            String username = req.getParameter("username");
            String password = req.getParameter("password");
            String role = req.getParameter("role");

            if (userDAO.findByUsername(username) == null) {
                userDAO.addUser(new User(username, UserDAO.hashPassword(password), role, true));
                session.setAttribute("successMessage", "ユーザーを追加しました。");
            } else {
                req.setAttribute("errorMessage", "ユーザーIDは既に存在します。");
            }

        } else if ("update".equals(action)) {
            // ユーザー更新
            String username = req.getParameter("username");
            String role = req.getParameter("role");
            boolean enabled = req.getParameter("enabled") != null;

            User existingUser = userDAO.findByUsername(username);
            if (existingUser != null) {
                userDAO.updateUser(new User(username, existingUser.getPassword(), role, enabled));
                session.setAttribute("successMessage", "ユーザー情報を更新しました。");
            }

        } else if ("toggle_enabled".equals(action)) {
            // 有効化・無効化
            String username = req.getParameter("username");
            boolean enabled = Boolean.parseBoolean(req.getParameter("enabled"));
            userDAO.toggleUserEnabled(username, enabled);
            session.setAttribute("successMessage", username + "のアカウントを" + (enabled ? "有効" : "無効") + "にしました。");

        } else if ("delete".equals(action)) {
            // ユーザー削除
            String username = req.getParameter("username");
            userDAO.deleteUser(username);
            session.setAttribute("successMessage", username + "を削除しました。");
        }

        // 最終的にユーザー一覧へ戻る
        resp.sendRedirect("users?action=list");
    }
}

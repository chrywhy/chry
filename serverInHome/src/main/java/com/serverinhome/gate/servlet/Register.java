package com.serverinhome.gate.servlet;

import com.serverinhome.resource.RigsterOkBean;
import java.io.*;
import javax.ejb.EJB;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import com.serverinhome.util.HTMLFilter;

@WebServlet(name="Register", urlPatterns={"/register"})
public class Register
    extends HttpServlet {
    
    @EJB
    private RigsterOkBean sless;

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        try {

            out.println("<HTML> <HEAD> <TITLE> Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1> Servlet2Stateless:: Please enter your name </FONT> </CENTER> <p> ");
            out.println("<form method=\"GET\">");
            out.println("<TABLE>");
            out.println("<tr><td>Name: </td>");
            out.println("<td><input type=\"text\" name=\"name\"> </td>");
            out.println("</tr><tr><td></td>");
            out.println("<td><input type=\"submit\" name=\"sub\"> </td>");
            out.println("</tr>");
            out.println("</TABLE>");
            out.println("</form>");
            String val = req.getParameter("name");
            
            if ((val != null) && (val.trim().length() > 0)) {
                out.println("<FONT size=+1 color=red> Greeting from StatelessSessionBean: </FONT>"
                            + HTMLFilter.filter(sless.sayHello(val)) + "<br>");
            }
            out.println("</BODY> </HTML> ");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("webclient servlet test failed");
            throw new ServletException(ex);
        }
    }
}

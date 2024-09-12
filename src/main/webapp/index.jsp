<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <title>Java 221</title>
  </head>
  <body>

  <h1>Java web</h1>
  <p>
    Java Server Pages - технологыя формування динамічних сторінок (сайтів)
    за допомогою серверної активності.
  </p>
  <h3>Вирази</h3>
  <p>
    Інструкції, що мають результат, причому мається на увазі, що цей результат
    стає частиною HTML.<br/>
    &lt;%= вираз мовою Java %&gt;<br/>
  </p>
  <h3>Інструкції</h3>
  <p>
    Директиви, що не мають результату, або результат яких ігнорується.
    &lt;% інструкції мовою Java %&gt;
    <br/>
    Наприклад, <code>&lt;% int x = 10; %&gt; <% int x = 10;%></code>
    <br/>
    <code>&lt;%= x %&gt; = <%= x%></code>
  </p>
  <h3></h3>
  <p>
    Умовне формування HTML коду, причому негативне плече умовного оператора взагалі не потрапляє до HTML.
    <br/>
    <pre>
    &lt;% if(true) { %&gt;
      HTML-якщо-true
    &lt;% } else { %&gt;
      HTML-якщо-false
    &lt;% } %&gt;
    </pre>
    <% if(x % 2==0) { %>
        <b>x - парне</b>
    <% } else { %>
        <i>x - непарне</i>
    <%}%>
  </p>
  <h3>Цикли</h3>
  <p>
    Повторне включення до HTML однакових (або майже) блоків верстки

  </p>
  <pre>
  &lt;% for(int i = 0; i < 5; i++) { %&gt;
    HTML, що повторюється &lt;%= i %&gt;
  &lt;% } %&gt;
</pre>
  <% for(int i = 0; i < 5; i++) { %>
    <span><%= i %></span>&emsp;
  <% } %>


  <%
    String[] arr = {"John", "Dean", "Sam", "Cas", "Jack"};

  %>
  <ul>
  <% for(String str : arr) { %>
    <li><%= str %></li>
  <% } %>
  </ul>

  <h3>Взаємодія з файлами HTML/JSP</h3>
  <p>
    Реалізація відображення одного файлу як частини іншого файлу.
  </p>
  &lt;% jsp:include page="fragment.jsp" %&gt;
  <jsp:include page="fragment.jsp"/>
  <pre>
    Browser                 Tomcat
     (URL)               (Listen:8080)      CGI
      HTTP->8080  -->    Parse   [Req, Resp] -> [python.exe index.py]
        <--------------- HTTP ---------------->     print '< html> .... Hello ..... < /html>'
  </pre>
  </body>
</html>

<%@ page import="java.util.Objects" %>
<%@ page import="java.util.List" %>
<%@ page import="itstep.learning.models.helpers.Product" %>
<%@ page contentType="text/html;charset=UTF-8"%>
<h1>Home</h1>
<a href="servlets">Servlets</a>

<div>
    <%=request.getAttribute("access")%>
</div>

<div>
    <%=request.getAttribute("hash")%>
</div>

<table class="my-table">
    <tbody>
    <tr>
        <th>
            id
        </th>
        <th>
            name
        </th>
        <th>
            price
        </th>
    </tr>

    <%
        List<Product> list = (List<Product>)request.getAttribute("productList");
        for(Product product: list){ %>
            <tr>
                <td>
                    <%= product.getId()%>
                </td>
                <td>
                    <%= product.getName()%>
                </td>
                <td>
                    <%= product.getPrice()%>
                </td>
            </tr>
    <% } %>

    </tbody>
</table>


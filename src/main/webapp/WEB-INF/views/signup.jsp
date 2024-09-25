<%@ page contentType="text/html;charset=UTF-8" %>
<h2>Реєстрація користувача</h2>

<%
    String contextPath = request.getContextPath();
%>

<div id="user-data">

</div>


<form id="signup-form"
      class="card-panel grey lighten-5"
      enctype="multipart/form-data"
      action="<%=contextPath%>/signup"
      method="post">
    <div class="row">
                <div class="input-field col s6">
                    <i class="material-icons prefix">badge</i>
                    <input id="user_name" name="user_name" type="text" class="validate">
                    <label for="user_name">Ім'я</label>
                </div>
                <div class="input-field col s6">
                    <i class="material-icons prefix">calendar_month</i>
                    <input id="user_phone" name="user_birth" type="date" class="validate">
                    <label for="user_phone">ДР</label>
                </div>
    </div>

    <div class="row">
        <div class="input-field col s6">
            <i class="material-icons  prefix">mail</i>
            <input id="user_email" name="user_email" type="email" class="validate">
            <label for="user_email">E-mail</label>
        </div>
        <div class="file-field input-field col s6">
            <div class="btn cyan">
                <i class="material-icons">account_circle</i>
                <input type="file" name="user_avatar">
            </div>
            <div class="file-path-wrapper">
                <input class="file-path validate" type="text">
            </div>
        </div>
    </div>

    <div class="row">
        <div class="input-field col s6">
            <i class="material-icons prefix">lock</i>
            <input id="user_pass" name="user_pass" type="password" class="validate">
            <label for="user_pass">Пароль</label>
        </div>
        <div class="input-field col s6">
            <i class="material-icons prefix">lock_open</i>
            <input id="user_pass_confirm" name="user_pass_confirm" type="password" class="validate">
            <label for="user_pass_confirm">Повтор</label>
        </div>
    </div>

    <div class="row">
        <button class="btn waves-effect waves-light cyan darken-2 right" type="submit">Зареєструватися
            <i class="material-icons right">send</i>
        </button>
    </div>

</form>

<div style="height: 40px"></div>
<h2>Розбір даних форм</h2>
<p>
    Форми передаються двома видами представлень:
    <code>application/x-www-form-urlencoded</code>
    <code>multipart/form-data</code>


    Перший включає лише поля (ключ=значенн) та може бути як в query-параметрах
    так і в тілі пакету.

    Другий може передавати файли і має значно складнішу структуру:
    multipart - такий, що складається з кількох частин, кожна з яких - це
    самостійний HTTP пакет, тільки без статус-рядка. Кожне поле форми передається
    окремою частиною, яка своїми заголовками визначає, що це - файл або поле.
</p>

<pre>
    POST /signup HTTP/1.1
    Connection: close;
    Delimiter: 1234;

    1234--
    Content-type: text/plain;
    Content-disposition: form-field; name=user-name

    Петрович

    1234--
    Content-type: text/plain;
    Content-disposition: form-field; name=user-email

    user_petrovich@gmail.com

    1234--
    Content-type: text/png;
    Content-disposition: attachment; filename=photo.png

    PNG21:kadjfl_dsajsf039a;

    --1234--


</pre>

<div style="height: 40px"></div>
<%@ page contentType="text/html;charset=UTF-8" %>
<h2>Реєстрація користувача</h2>
<form>
    <div class="row">
                <div class="input-field col s6">
                    <i class="material-icons prefix">badge</i>
                    <input id="user_name" type="text" class="validate">
                    <label for="user_name">Ім'я</label>
                </div>
                <div class="input-field col s6">
                    <i class="material-icons prefix">phone</i>
                    <input id="user_phone" type="tel" class="validate">
                    <label for="user_phone">Телефон</label>
                </div>
    </div>

    <div class="row">
        <div class="input-field col s6">
            <i class="material-icons  prefix">mail</i>
            <input id="user_email" type="email" class="validate">
            <label for="user_email">E-mail</label>
        </div>
        <div class="file-field input-field col s6">
            <div class="btn cyan">
                <i class="material-icons">account_circle</i>
                <input type="file">
            </div>
            <div class="file-path-wrapper">
                <input class="file-path validate" type="text">
            </div>
        </div>
    </div>

    <div class="row">
        <div class="input-field col s6">
            <i class="material-icons prefix">lock</i>
            <input id="user_pass" type="password" class="validate">
            <label for="user_pass">Пароль</label>
        </div>
        <div class="input-field col s6">
            <i class="material-icons prefix">lock_open</i>
            <input id="user_pass_confirm" type="password" class="validate">
            <label for="user_pass_confirm">Повтор</label>
        </div>
    </div>

    <div class="row">
        <button class="btn waves-effect waves-light cyan darken-2 right" type="submit" name="action">Зареєструватися
            <i class="material-icons right">send</i>
        </button>
    </div>

</form>
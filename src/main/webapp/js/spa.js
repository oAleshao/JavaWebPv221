function Spa(){

    const [login, setLogin] = React.useState("");
    const [password, setPassword] = React.useState("");
    const loginChange = React.useCallback((e)=> setLogin(e.target.value));
    const passwordChange = React.useCallback((e)=> setPassword(e.target.value));

    const authClick = React.useCallback(()=> {
        const credentials = btoa(login + ":" + password);
        fetch(`auth`, {
            method: "GET",
            headers: {
                'Authorization': 'Basic ' + credentials
            }
        }).then(r => r.json()).then(console.log);
        console.log(credentials)
    });


    return (<React.Fragment>
        <h1>Spa</h1>
        <div>
            <b>Login</b>
            <input placeholder="login" onChange={loginChange}/>
            <b>Password</b>
            <input type="password" onChange={passwordChange} placeholder="password"/>
            <button onClick={authClick}>Get Token</button>
        </div>
    </React.Fragment>)
}


const root = ReactDOM.createRoot(document.getElementById("spa-container"));
root.render(
    <Spa />
);

const initialState = {
    page: "home",
    auth: {
        token: null
    },
    shop: {
        categories: []
    }
}

function reducer(state, action) {
    switch (action.type) {
        case "auth":
            return {
                ...state,
                auth: {
                    ...state.auth,
                    token: action.payload
                }
            }
        case "navigate":
            window.location.hash = action.payload;
            return {
                ...state,
                page: action.payload,
            }
        case "setCategory":
            return {
                ...state,
                shop: {
                    ...state.shop,
                    categories: action.payload
                }
            }
        default:
            throw new Error("unkonwn action");
    }
}

const StateContext = React.createContext();

function Spa() {
    const [state, dispatch] = React.useReducer(reducer, initialState);
    const [login, setLogin] = React.useState("");
    const [password, setPassword] = React.useState("");
    const [error, setError] = React.useState(false);
    const [isAuth, setAuth] = React.useState(false);
    const [resource, setResource] = React.useState("");


    const loginChange = React.useCallback((e) => setLogin(e.target.value));
    const passwordChange = React.useCallback((e) => setPassword(e.target.value));
    const authClick = React.useCallback(() => {
        const credentials = btoa(login + ":" + password);
        fetch(`auth`, {
            method: "GET",
            headers: {
                'Authorization': 'Basic ' + credentials
            }
        }).then(r => r.json()).then(j => {
                if (j.status === "OK") {
                    window.sessionStorage.setItem("token_pv221", JSON.stringify(j.data));
                    setAuth(true);
                } else {
                    setError(j.data);
                }
            }
        );
    });
    const resourceClick = React.useCallback(() => {
        const token = window.sessionStorage.getItem("token_pv221");
        if (!token) {
            alert("Something wrong with authorization");
            return;
        }

        fetch("spa", {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + JSON.parse(token).tokenId,
            }
        }).then(r => r.json()).then(j => {
            // if(j.status === "Error"){
            //     window.sessionStorage.removeItem("token_pv221");
            //     setAuth(false);
            // }
            setResource(JSON.stringify((j)));
        });


    });
    const exitClick = React.useCallback(() => {
        window.sessionStorage.removeItem("token_pv221");
        setAuth(false);
    });
    const checkToken = React.useCallback(() => {
        let token = window.sessionStorage.getItem("token_pv221");
        if (token) {
            token = JSON.parse(token);
            if (new Date(token.exp) < new Date()) {
                exitClick();
            } else {
                if (!isAuth) {
                    setAuth(true);
                    dispatch({type: "auth", payload: token})
                }
            }
        } else {
            setAuth(false);
        }
    })
    const navigate = React.useCallback((root) => {
        const action = {type: "navigate", payload: root};
        dispatch(action);
    });


    React.useEffect(() => {

        const hash = window.location.hash;
        if(hash.length > 1){
            dispatch({type: "navigate", payload: hash.substring(1)});
        }

        checkToken();
        const interval = setInterval(checkToken, 1000);
        return () => clearInterval(interval);
    }, []);


    return (<StateContext.Provider value={{state, dispatch}}>
        <h1>Spa</h1>
        {!isAuth &&
            <div>
                <b>Login</b>
                <input placeholder="login" onChange={loginChange}/>
                <b>Password</b>
                <input type="password" onChange={passwordChange} placeholder="password"/>
                <button onClick={authClick}>Get Token</button>
                {error && <b>{error}</b>}
            </div>
        }
        {isAuth &&


            <div>
                <div>
                    <button onClick={resourceClick} className="btn cyan">Resource</button>
                    <p>{resource}</p>
                    <button onClick={exitClick} className="btn blue lighted-3">Exit</button>
                </div>
                <b onClick={() => {
                    navigate('home')
                }}>Home</b>

                <b onClick={() => {
                    navigate('shop')
                }}>Shop</b>

                {state.page === "home" && <Home  key={"UNIQUE HOME"}/>}
                {state.page === "shop" && <Shop key={"UNIQUE SHOP"}/>}
                {state.page.startsWith('category/') && <Category id={state.page.substring(9)}/>}
            </div>
        }
    </StateContext.Provider>)
}

function Home() {
    const {state, dispatch} = React.useContext(StateContext);
    React.useEffect(() => {
        if (state.shop.categories.length === 0) {
            fetch("shop/category")
                .then(r => r.json())
                .then(j => dispatch({type: "setCategory", payload: j.data}));
        }
    }, []);
    return <React.Fragment>
        <h2>Home</h2>
        <b onClick={() => dispatch({type: "navigate", payload: "shop"})}>Log in as admin</b>
        <div>
            {state.shop.categories.map(c =>
                <div className="shop-category" key={c.id}
                     onClick={() => dispatch({type: "navigate", payload: "category/" + c.id})}>
                    <b>{c.name}</b>
                    <img src={"file/" + c.imageUrl} alt={c.name}/>
                    <p>{c.description}</p>
                </div>)
            }
        </div>
    </React.Fragment>
}

function Category({id}) {
    const {state, dispatch} = React.useContext(StateContext);

    const addProduct = React.useCallback((e)=>{
        e.preventDefault();
        const formData = new FormData(e.target);
        fetch("shop/product", {
            method: "POST",
            headers: {
                "Authorization": "Bearer " + state.auth.token.tokenId
            },
            body: formData
        }).then(r => r.json()).then(console.log);
    });

    return <div>
        Category: {id}<br/>
        <b onClick={() => dispatch({type: "navigate", payload: "home"})}>Go back</b>
        {state.auth.token &&
            <form onSubmit={addProduct} encType="multipart/form-data">
                <hr/>
                <input type="hidden" name="product_category" value={id}/>
                <input name="product_name" placeholder="Name of product"/>
                <input name="product_slug" placeholder="Slug"/>
                <br/>
                <input name="product_price" type="number" step="0.01" placeholder="Price"/>
                <br/>
                Picture: <input type="file" name="product_img"/>
                <br/>
                <textarea name="product_description" placeholder="Description"></textarea>
                <button type="submit">Add new product</button>
            </form>

        }
    </div>
}

function Shop() {
    const addCategory = React.useCallback( (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        fetch("shop/category", {
            method: 'POST',
            body: formData
        }).then(r => r.json()).then(console.log);
        // console.log(e);
    });

    return <React.Fragment>
        <h2>Shop</h2>
        <hr/>
        <form onSubmit={addCategory} encType="multipart/form-data">
            <input name="category_name" placeholder="Category"/>
            <input name="category_slug" placeholder="Slug"/><br/>
            Картинка: <input type="file" name="category_img"/><br/>
            <textarea name="category_description" placeholder="Description"></textarea><br/>
            <button type="submit">Додати</button>
        </form>
    </React.Fragment>;
}


const root = ReactDOM.createRoot(document.getElementById("spa-container"));
root.render(
    <Spa/>
);

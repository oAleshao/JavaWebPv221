﻿const env = {
    apiHost: "http://localhost:8080/Java221"
};


const initialState = {
    page: "home",
    auth: {
        token: null,
        user: null,
        user_role: null,
        tmpId: null
    },
    shop: {
        categories: []
    },
    cart: [],
    error: null
}

function reducer(state, action) {
    switch (action.type) {
        case "auth":
            window.localStorage.removeItem("tmpId_pv221");
            return {
                ...state,
                auth: {
                    ...state.auth,
                    token: action.payload,
                    tmpId: null
                }
            }
        case "auth-tmp":
            return {
                ...state,
                auth: {
                    ...state.auth,
                    tmpId: action.payload
                }
            }
        case "updateUser":
            return {
                ...state,
                auth: {
                    ...state.auth,
                    user: action.payload
                }
            }
        case "setUserRole":
            return {
                ...state,
                auth: {
                    ...state.auth,
                    user_role: action.payload
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
        case "setCart":
            return {
                ...state,
                cart: action.payload
            }
        case "setError":
            return {
                ...state,
                error: action.payload
            }
        default:
            throw new Error("unkonwn action");
    }
}

const StateContext = React.createContext();

function uuidv4() {

    return "10000000-1000-4000-8000-100000000000".replace(/[018]/g, c =>

        (+c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> +c / 4).toString(16)
    );

}

function Spa() {
    const [state, dispatch] = React.useReducer(reducer, initialState);
    const [resource, setResource] = React.useState("");


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
        window.sessionStorage.removeItem("userRole_pv221");
        window.sessionStorage.removeItem("user_pv221");
        window.location.reload();
    });
    const userLogOut = React.useCallback(() => {
        document.querySelector("div.helper-box-exit").classList.toggle("nonactiveBox");
    });
    const checkToken = React.useCallback((forceAuth) => {
        let token = window.sessionStorage.getItem("token_pv221");
        if (token) {
            token = JSON.parse(token);
            if (new Date(token.exp) < new Date()) {
                exitClick();
            } else {
                if (forceAuth) {
                    dispatch({type: "auth", payload: token})
                }
            }
        } else {
            let tmpId = window.localStorage.getItem("tmpId_pv221")
            if (tmpId != null) {
                state.auth.tmpId = tmpId;
                if (forceAuth) {
                    // dispatch({type: "auth-tmp", payload: tmpId});
                }
            }
        }
    })
    const navigate = React.useCallback((root) => {
        const action = {type: "navigate", payload: root};
        dispatch(action);
    });
    const hashWindow = React.useCallback(() => {
        const hash = window.location.hash;
        if (hash.length > 1) {
            dispatch({type: "navigate", payload: hash.substring(1)});
        }
    })
    const loadCart = React.useCallback(() => {
        request("/shop/cart")
            .then(data => dispatch({type: "setCart", payload: data}))
            .catch(console.log)
    });
    const request = React.useCallback((url, params) => {

        if (url.startsWith('/')) {
            url = env.apiHost + url;
        }

        let bearer = null;
        if (state.auth.token != null) {
            bearer = state.auth.token.tokenId
        } else if (state.auth.tmpId != null) {
            bearer = state.auth.tmpId;
        }


        if (bearer) {
            if (typeof params == "undefined") {
                params = {};
            }

            if (typeof params.headers == "undefined") {
                params.headers = {
                    Authorization: "Bearer " + bearer
                }
            } else if (typeof params.headers.Authorization == "undefined") {
                params.headers.Authorization = "Bearer " + bearer
            }
        }


        return new Promise((resolve, reject) => {
            fetch(url, params)
                .then(r => r.json())
                .then(j => {
                    if (j.status.isSuccessful) {
                        resolve(j.data);
                    } else {
                        reject(j.data);
                    }
                });
        });
    });
    const getUser = React.useCallback((tokenId) => {
        fetch('auth', {
            method: "GET",
            headers: {
                'Authorization': 'UserGet ' + tokenId
            }
        })
            .then(r => r.json())
            .then(j => {
                window.sessionStorage.setItem("user_pv221", JSON.stringify(j.data));
                dispatch({type: "updateUser", payload: j.data})
                getUserRole(j.data.roleId);
            })
    });
    const getUserRole = React.useCallback((roleId) => {
        fetch('auth', {
            method: "GET",
            headers: {
                'Authorization': 'UserRoleGet ' + roleId
            }
        })
            .then(r => r.json())
            .then(j => {
                window.sessionStorage.setItem("userRole_pv221", JSON.stringify(j.data));
                dispatch({type: "setUserRole", payload: j.data})
                dispatch({type: "navigate", payload: "home"})
                window.location.reload();
            })
    });
    const authClick = React.useCallback((login, password) => {
        const credentials = btoa(login + ":" + password);
        let endpoint = "/auth";
        if (state.auth.tmpId != null) {
            endpoint += "?tmp-id=" + state.auth.tmpId;
        }

        request(endpoint, {
            method: "GET",
            headers: {
                'Authorization': 'Basic ' + credentials
            }
        }).then(data => {
                window.sessionStorage.setItem("token_pv221", JSON.stringify(data));
                getUser(data.tokenId);
        }).catch(console.error);
    });


    React.useEffect(() => {
        hashWindow();
        window.addEventListener("hashchange", hashWindow)
        checkToken(true);

        let token = window.sessionStorage.getItem("token_pv221");
        if (token) {
            token = JSON.parse(token);
            getUser(token.tokenId);
        }

        const interval = setInterval(() => {
            checkToken(false)
        }, 1000);

        if (state.shop.categories.length === 0) {
            fetch("shop/category")
                .then(r => r.json())
                .then(j => dispatch({type: "setCategory", payload: j.data}));
        }

        return () => {
            clearInterval(interval);
            window.removeEventListener('hashchange', hashWindow);
        }
    }, []);
    React.useEffect(() => {
        if (state.auth.token != null || state.auth.tmpId != null) {
            loadCart();
        } else {
            dispatch({type: "setCart", payload: []})
        }
    }, [state.auth])
    return (<StateContext.Provider value={{state, dispatch, loadCart, request, authClick}}>
        <h1>Spa</h1>
        <div>
            {state.auth.user &&
                <div className={"buttons-block"}>
                    <button onClick={resourceClick} className="btn cyan">Resource</button>
                    <p>{resource}</p>
                    <button id={"exitBtn"} onClick={userLogOut} className="btn blue lighted-3">Exit</button>
                    <div className={"helper-box-exit nonactiveBox"}>
                        <div>Are you sure you want to log out?</div>
                        <div>
                            <button onClick={exitClick}>Log out</button>
                            <button onClick={userLogOut}>Cancel</button>
                        </div>
                    </div>

                    <div className={"auth-user-box"}>
                        <p>{state.auth.user.name}</p>
                        <img src={"file/" + state.auth.user.avatar} alt={state.auth.user.avatar}/>
                    </div>

                </div>
            }

            {!state.auth.user &&
                <b key={"btnAuth"} onClick={() => {
                    navigate('authPage')
                }}>Log in</b>}&emsp;

            <b key={"btnHome"} onClick={() => {
                navigate('home')
            }}>Home</b>&emsp;

            {state.auth.user && state.auth.user_role && state.auth.user_role.canCreate &&
                <b key={"btnShop"} onClick={() => {
                    navigate('shop')
                }}>Shop</b>
            }&emsp;

            <b onClick={() => {
                navigate('cart')
            }}>Cart({state.cart.reduce((cnt, c) => cnt + c.quantity, 0)} ${state.cart.reduce((price, c) => price + c.quantity * c.product.price, 0)})</b>&emsp;

            {state.page === "authPage" && <AuthPage/>}
            {state.page === "home" && <Home key={"UNIQUE HOME"}/>}
            {state.page === "shop" && state.auth.user_role && state.auth.user_role.canCreate &&
                <Shop key={"UNIQUE SHOP"}/>}
            {state.page === "cart" && <Cart/>}
            {state.page.startsWith('category/') && <Category id={state.page.substring(9)}/>
            }
            {
                state.page.startsWith('product/') && <Product id={state.page.substring(8)}/>
            }

        </div>

    </StateContext.Provider>)
}

function AuthPage() {
    const {state, authClick} = React.useContext(StateContext);
    const [login, setLogin] = React.useState("");
    const [password, setPassword] = React.useState("");

    const loginChange = React.useCallback((e) => setLogin(e.target.value));
    const passwordChange = React.useCallback((e) => setPassword(e.target.value));

    return <div>
        <b>Login</b>
        <input placeholder="login" onChange={loginChange}/>
        <b>Password</b>
        <input type="password" onChange={passwordChange} placeholder="password"/>
        <button onClick={() => {
            authClick(login, password)
        }}>Get Token
        </button>
        {state.error && <b> {state.error} </b>}
    </div>
}

function Home() {
    return <React.Fragment>
        <h2>Home</h2>
        <b onClick={() => dispatch({type: "navigate", payload: "shop"})}>Log in as admin</b>
        <CategoriesList mode={"table"}/>
    </React.Fragment>
}

function CategoriesList({mode}) {
    const {state, dispatch} = React.useContext(StateContext);
    return <React.Fragment>
        {mode === "table" &&
            <div>
                {state.shop.categories.map(c =>
                    <div className="shop-category" key={c.id}
                         onClick={() => dispatch({type: "navigate", payload: "category/" + (c.slug || c.id)})}>
                        <b>{c.name}</b>
                        <img src={"file/" + c.imageUrl} alt={c.name}/>
                        <p>{c.description}</p>
                    </div>)
                }
            </div>
        }
        {mode === "ribbon" &&
            <div className={"category-ribbon"}>
                <h4>More categories...</h4>
                {state.shop.categories.map(c =>
                    <div className="shop-category-ribbon-item" key={c.id} title={c.name}
                         onClick={() => dispatch({type: "navigate", payload: "category/" + (c.slug || c.id)})}>
                        <img src={"file/" + c.imageUrl} alt={c.name}/>
                    </div>)
                }
            </div>
        }
    </React.Fragment>
}

function Product({id}) {
    const [product, setProduct] = React.useState(null);
    React.useEffect(() => {
        request(`/shop/product?id=${id}`)
            .then(setProduct)
            .catch(error => {
                console.error(error);
                setProduct(null);
            })
        // fetch("shop/product?id=" + id)
        //     .then(r => r.json())
        //     .then(j => {
        //         if (j.status.isSuccessful) {
        //             setProduct(j.data);
        //         } else {
        //             console.log(j.data);
        //             setProduct(null);
        //         }
        //     })
    }, [id]);
    return <div>
        <h1>Page of product</h1>
        {product && <div>
            <p>{product.name}</p>
        </div>
        }
        {!product && <div>
            <b>We are looking for...</b>
        </div>
        }

        <CategoriesList mode={"ribbon"}/>
    </div>
}

function Category({id}) {
    const {state, dispatch, loadCart, request} = React.useContext(StateContext);
    const [products, setProducts] = React.useState(null);
    const addProduct = React.useCallback((e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        fetch("shop/product", {
            method: "POST",
            headers: {
                "Authorization": "Bearer " + state.auth.token.tokenId
            },
            body: formData
        }).then(r => r.json()).then(j => {
            if (j.status.isSuccessful) {
                loadProducts();
                document.getElementById("productForm").reset();
            } else {
                alert(j.data);
            }
        });
    });
    const loadProducts = React.useCallback(() => {
        request(`shop/product?categoryId=${id}`)
            .then(setProducts)
            .catch(error => {
                console.error(error);
                setProducts(null);
            })
    });
    const addProductToCart = React.useCallback((id) => {
        let userId;
        let bearer;

        if (state.auth.token === null) {
            if (state.auth.tmpId == null) {
                state.auth.tmpId = uuidv4();
                window.localStorage.setItem("tmpId_pv221", state.auth.tmpId);
            }
            bearer = state.auth.tmpId;
            userId = state.auth.tmpId;
        } else {
            bearer = state.auth.token.tokenId
            userId = state.auth.token.userId;
        }

        request("/shop/cart", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                userId: userId,
                productId: id,
                quantity: 1
            })
        }).then(() => loadCart())
            .catch(console.log)


    });
    React.useEffect(() => {
        loadProducts();
    }, [id]);

    return <div>
        {products && <div>
            Category: {id}<br/>

            <div key="products-box">
                {
                    products.map(p =>
                        <div key={p.id} className="shop-product"
                             onClick={() => dispatch({type: "navigate", payload: "product/" + (p.slug || p.id)})}>
                            <b>{p.name}</b>
                            <picture>
                                <img src={"file/" + p.imageUrl} alt="prod"/>
                            </picture>
                            <div className={"row"}>
                                <div className={"col s9"}>
                                    <strong>{p.price}</strong> <small>{p.description}</small>

                                </div>
                                <div className={"col s3"}>
                                    <a className="btn-floating cart-fab waves-effect waves-light red"
                                       title={"add to cart"}
                                       onClick={(e) => {
                                           e.stopPropagation();
                                           addProductToCart(p.id)
                                       }}><i
                                        className="material-icons">add</i></a>
                                </div>
                            </div>

                        </div>)
                }
            </div>


            {state.auth.user_role && state.auth.user_role.canCreate &&
                <div>
                    <b onClick={() => dispatch({type: "navigate", payload: "home"})}>Go back</b>

                    <form id="productForm" onSubmit={addProduct} encType="multipart/form-data">
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
                </div>
            }
        </div>
        }
        {!products && <div>Not founded category</div>}
    </div>
}

function Shop() {
    const addCategory = React.useCallback((e) => {
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

function Cart() {
    const {state, dispatch, loadCart, request} = React.useContext(StateContext);
    const updateQuantity = React.useCallback((cartItem, delta) => {
        if (+cartItem.quantity + delta === 0) {
            if (!confirm(`Do your really want to delete '${cartItem.product.name}' from cart?`)) {
                return;
            }
        }
        request("/shop/cart", {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                cartId: cartItem.cartId,
                productId: cartItem.productId,
                delta: delta
            })
        }).then(() => loadCart())
            .catch(console.log)
    });
    const changeQuantity = React.useCallback((cartItem, action) => {
        switch (action) {
            case "inc":
                updateQuantity(cartItem, 1);
                break;
            case "dec":
                updateQuantity(cartItem, -1);
                break;
            case "del":
                updateQuantity(cartItem, -cartItem.quantity);
                break;
        }
    });
    const closeCart = React.useCallback((isCanceled) => {
        if (state.auth.token == null && !isCanceled) {
            // the shopping cart is in unauthorized mode
            if (confirm("You buy in anonymous mode. The history " +
                "of purchases will not be saved, personal discounts are not taken into account." +
                "Do you want to log in?")) {
                dispatch({type: "navigate", payload: "authPage"});
                return;
            }
        }

        if (!isCanceled) {
            if (!confirm(`Buy everything for the amount $${state.cart.reduce((price, c) => price + c.quantity * c.product.price, 0)}`)) {
                return;
            }
        } else {
            if (!confirm("Do you really want to clear your cart!")) {
                return;
            }
        }
        request(`/shop/cart?cart-id=${state.cart[0].cartId}&is-canceled=${isCanceled}`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json"
            }
        }).then(() => loadCart())
            .catch(console.log)

    });
    return <React.Fragment>
        <h1>Cart</h1>
        {state.cart.length > 0 && <div>
            <button onClick={() => {
                closeCart(true)
            }}>Clear cart
            </button>
            <div className={"cart-box"}>
                {
                    state.cart.map(c =>
                        <div key={c.productId} className={"cart-item"}>
                            <div className={"cart-img-box"}>
                                <img src={"file/" + c.product.imageUrl} alt={c.productId + "_img"}/>
                            </div>
                            <div className={"cart-item-info"}>
                                <div className={"product-name"}>{c.product.name}</div>
                                <div className={"counter-box"}>
                                    <section className={"quantity-box"}>
                                        <div>
                                            <button onClick={() => {
                                                changeQuantity(c, "dec")
                                            }}>-
                                            </button>
                                        </div>
                                        <div>
                                            {c.quantity}
                                        </div>
                                        <div>
                                            <button onClick={() => {
                                                changeQuantity(c, "inc")
                                            }}>+
                                            </button>
                                        </div>
                                    </section>
                                    <section className={"price-box"}>
                                        ${c.product.price * c.quantity}
                                    </section>
                                </div>
                                <div className={"remove-box"}>
                                    <button onClick={() => {
                                        changeQuantity(c, "del")
                                    }}>
                                        <i className="material-symbols-outlined">Remove</i>
                                    </button>
                                </div>
                            </div>
                        </div>)
                }
            </div>
            <hr/>
            <div className={"total-price-box"}>
                Total amount: ${state.cart.reduce((price, c) => price + c.quantity * c.product.price, 0)}
                <button onClick={() => {
                    closeCart(false)
                }}>Buy
                </button>
            </div>
        </div>
        }
        {state.cart.length < 1 &&
            <div className={"empty-cart"}>
                Your cart is empty):
                <br/>
                You can fix it!!!
            </div>
        }
    </React.Fragment>
}


const root = ReactDOM.createRoot(document.getElementById("spa-container"));
root.render(<Spa/>);
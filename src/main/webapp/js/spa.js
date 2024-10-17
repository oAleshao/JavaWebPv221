const env = {
    apiHost: "http://localhost:8080/Java221"
};

async function request(url, params) {

    if (url.startsWith('/')) {
        url = env.apiHost + url;
    }

    // if (typeof params.headers == "undefined") {
    //     params = {
    //         ...params,
    //         headers:{
    //             Authorization: "Bearer " +
    //         }
    //     }
    // } else if (typeof params.headers.Authorization == "undefined") {
    //
    // }

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


}


const initialState = {
    page: "home",
    auth: {
        token: null,
        user: null,
        user_role: null
    },
    shop: {
        categories: []
    },
    cart: []
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
            })
    });
    const authClick = React.useCallback(() => {
        const credentials = btoa(login + ":" + password);
        fetch(`auth`, {
            method: "GET",
            headers: {
                'Authorization': 'Basic ' + credentials
            }
        }).then(r => r.json()).then(j => {
                if (j.status.isSuccessful) {
                    window.sessionStorage.setItem("token_pv221", JSON.stringify(j.data));
                    getUser(j.data.tokenId);
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
    const hashWindow = React.useCallback(() => {
        const hash = window.location.hash;
        if (hash.length > 1) {
            dispatch({type: "navigate", payload: hash.substring(1)});
        }
    })
    const loadCart = React.useCallback(() => {
        request("/shop/cart", {
            headers: {
                Authorization: "Bearer " + state.auth.token.tokenId,
                "Content-Type": "application/json"
            },
        }).then(data => dispatch({type: "setCart", payload: data}))
            .catch(console.log)
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
        if (state.auth.token != null) {
            loadCart();
        } else {
            dispatch({type: "setCart", payload: []})
        }
    }, [state.auth])

    return (<StateContext.Provider value={{state, dispatch, loadCart}}>
        <h1>Spa</h1>
        {!isAuth &&
            <div>
                <b>Login</b>
                <input placeholder="login" onChange={loginChange}/>
                <b>Password</b>
                <input type="password" onChange={passwordChange} placeholder="password"/>
                <button onClick={authClick}>Get Token</button>
                {error && <b> {error} </b>}
            </div>
        }
        {isAuth &&
            <div>
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
                    {state.auth.user &&
                        <div className={"auth-user-box"}>
                            <p>{state.auth.user.name}</p>
                            <img src={"file/" + state.auth.user.avatar} alt={state.auth.user.avatar}/>
                        </div>
                    }
                </div>

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
        }
    </StateContext.Provider>)
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
    const {state, dispatch, loadCart} = React.useContext(StateContext);
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
        console.log(id);
        const userId = state.auth.token.userId;

        request("/shop/cart", {
            method: "POST",
            headers: {
                Authorization: "Bearer " + state.auth.token.tokenId,
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
    const {state, dispatch, loadCart} = React.useContext(StateContext);
    const changeQuantity = React.useCallback((cartItem, action) => {
        switch (action) {
            case "inc":
                console.log(cartItem, action);
                break;
            case "dec":
                console.log(cartItem, action);
                break;
            case "del":
                console.log(cartItem, action);
                break;
        }
        loadCart();
    });
    return <React.Fragment>
        <h1>Cart</h1>
        {state.cart.length > 0 && <div>
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
                                            <button>-</button>
                                        </div>
                                        <div>
                                            {c.quantity}
                                        </div>
                                        <div>
                                            <button>+</button>
                                        </div>
                                    </section>
                                    <section className={"price-box"}>
                                        ${c.product.price * c.quantity}
                                    </section>
                                </div>
                                <div className={"remove-box"}>
                                    <button>
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
document.addEventListener("submit", e =>{
    const form = e.target;
   if(form.id === "signup-form"){
       e.preventDefault();
       const formData = new FormData(form);
       fetch(form.action, {
           method: "POST",
           body: formData
       }).then(r=>r.json()).then(j=> putUserData(j.data));
   }
   else if(form.id === "modal-auth-form"){
       e.preventDefault();
       const queryString = new URLSearchParams(new FormData(form)).toString()
       console.log(queryString);
       fetch(`${form.action}?${queryString}`, {
           method: "PATCH",
       }).then(r=>r.json()).then( j=>{
           if(j.status === "OK"){
               window.location.reload();
           }
           else {
               console.log(j);
           }
       });
   }

});

function putUserData(data){
    const box = document.getElementById("user-data");

    let innerHTML = `<b>Name: ${data["name"]}</b><br/><b>Email: ${data["email"]}</b><br/><b>Birhday: ${data["birthday"]}</b><br/><b>Avatar: ${data["avatar"]}</b><br/><b>Password: ${data["password"]}</b><br/>`;

    box.innerHTML = innerHTML;
}

document.addEventListener('DOMContentLoaded', function() {
    M.Modal.init(
        document.querySelectorAll('.modal'), {
        opacity: 0.5,
        inDuration: 250,
        outDuration: 250,
        onOpenStart: null,
        onOpenEnd: null,
        onCloseStart: null,
        onCloseEnd: null,
        preventScrolling: true,
        dismissible: true,
        startingTop: '4%',
        endingTop: '10%',
    });
});
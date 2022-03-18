function main() {
    let button = document.getElementById("list-all-users");
    let button1 = document.getElementById("find-user");
    let button2 = document.getElementById("add-user");
    button.addEventListener('click', loadUsers);
    button1.addEventListener('click' , loadUser)
    button2.addEventListener('click', addUser);

}

async function loadUsers() {
    let ul = document.getElementById("users");
    ul.innerHTML = "";
    fetch('http://localhost:8200/app')
     .then(res => res.json())
     .then(users => {
         users.forEach(user => {
             let li = document.createElement("li");
             li.textContent = user.name + " " + user.age;
             ul.append(li);
         });
     })
     .catch(error => alert(error.toString()));
}

async function loadUser() {
    let name = document.getElementById("inputName").value;
    document.getElementById("inputName").value = "";
    fetch('http://localhost:8200/app/' + name)
        .then(res => res.json())
        .then(user => {
            document.getElementById("found-user")
                .textContent = user.name + " " + user.age;
        })
        .catch(error => alert(error.toString()));
}

async function addUser() {
    let name = document.getElementById("name").value;
    let age = document.getElementById("age").value;
    document.getElementById("name").value = "";
    document.getElementById("age").value = "";
    let body = {
        name: name,
        age: age
    };
    fetch('http://localhost:8200/app/', {
        method: "POST",
        body: JSON.stringify(body)
    }).then(() => loadUsers());
}
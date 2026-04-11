document.getElementById('loginForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const email = document.getElementById('usuario').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch('/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email, contrasena: password })
        });

        const data = await response.json();

        if (response.ok) {
            localStorage.setItem("token", data.token);
            localStorage.setItem('usuario', JSON.stringify(data));
            window.location.href = '/index';
        } else {
            alert(data.error);
        }
    } catch (error) {
        alert('Error al conectar con el servidor');
    }
});
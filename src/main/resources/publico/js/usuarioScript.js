let usuarioEditandoId = null;

// Cargar usuarios al iniciar
document.addEventListener('DOMContentLoaded', cargarUsuarios);

async function cargarUsuarios() {
    try {
        const response = await fetch('/api/usuarios');
        const usuarios = await response.json();
        renderizarUsuarios(usuarios);
    } catch (error) {
        console.error('Error al cargar usuarios:', error);
    }
}

function renderizarUsuarios(usuarios) {
    const lista = document.getElementById('listaUsuarios');
    lista.innerHTML = '';

    usuarios.forEach(usuario => {
        const fila = `
            <tr>
                <td>
                    <div class="fw-bold">${usuario.nombre}</div>
                    <div class="text-muted small">${usuario.email}</div>
                </td>
                <td>
                    <span class="badge ${usuario.rol === 'ADMIN' ? 'bg-primary' : 'bg-secondary'}">
                        ${usuario.rol}
                    </span>
                </td>
                <td class="text-end">
                    <button class="btn btn-sm btn-outline-warning me-1" onclick="editarUsuario('${usuario.id}')">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="eliminarUsuario('${usuario.id}')">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            </tr>
        `;
        lista.innerHTML += fila;
    });
}

// Crear o actualizar usuario
document.getElementById('usuarioForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const usuario = {
        nombre: document.getElementById('nombre').value,
        email: document.getElementById('email').value,
        contrasena: document.getElementById('passUsuario').value,
        rol: document.getElementById('rol').value
    };

    const esEdicion = usuarioEditandoId !== null;
    const url = esEdicion ? `/api/usuarios/${usuarioEditandoId}` : '/api/usuarios';
    const metodo = esEdicion ? 'PUT' : 'POST';

    try {
        const response = await fetch(url, {
            method: metodo,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(usuario)
        });

        const data = await response.json();

        if (response.ok) {
            alert(esEdicion ? 'Usuario actualizado correctamente' : 'Usuario creado correctamente');
            limpiarFormulario();
            cargarUsuarios();
        } else {
            alert(data.error || 'Ocurrió un error');
        }
    } catch (error) {
        alert(esEdicion ? 'Error al actualizar usuario' : 'Error al crear usuario');
    }
});

// Eliminar usuario
async function eliminarUsuario(id) {
    if (!confirm('¿Está seguro de eliminar este usuario?')) return;

    try {
        const response = await fetch(`/api/usuarios/${id}`, {
            method: 'DELETE'
        });

        const data = await response.json();

        if (response.ok) {
            alert('Usuario eliminado correctamente');
            cargarUsuarios();
        } else {
            alert(data.error);
        }
    } catch (error) {
        alert('Error al eliminar usuario');
    }
}

// Editar usuario
async function editarUsuario(id) {
    try {
        const response = await fetch(`/api/usuarios/${id}`);
        const usuario = await response.json();

        if (!response.ok) {
            alert(usuario.error || 'Error al cargar usuario');
            return;
        }

        document.getElementById('nombre').value = usuario.nombre;
        document.getElementById('email').value = usuario.email;
        document.getElementById('passUsuario').value = '';
        document.getElementById('rol').value = usuario.rol;

        usuarioEditandoId = id;

        document.getElementById('tituloFormulario').textContent = 'Editar Usuario';
        document.getElementById('textoBotonGuardar').textContent = 'Actualizar Usuario';

        const collapse = new bootstrap.Collapse(document.getElementById('formRegistrarUsuario'), {
            show: true
        });
    } catch (error) {
        alert('Error al obtener datos del usuario');
    }
}

function limpiarFormulario() {
    document.getElementById('usuarioForm').reset();
    usuarioEditandoId = null;
    document.getElementById('tituloFormulario').textContent = 'Registrar Usuario';
    document.getElementById('textoBotonGuardar').textContent = 'Guardar Usuario';
}
// =============================
// 1. VARIABLES
// =============================
const carrito = document.querySelector('#carrito');
const contenedorCarrito = document.querySelector('#lista-carrito tbody');
const vaciarCarritoBtn = document.querySelector('#vaciar-carrito');
const listaDestacados = document.querySelector('#lista-1');
const listaTodos = document.querySelector('#lista-todos');

let articulosCarrito = [];

// =============================
// 2. INICIO DE LA APP
// =============================
document.addEventListener("DOMContentLoaded", () => {
    obtenerProductos();
    cargarEventListeners();
});

// =============================
// 3. CONEXIÓN CON EL SERVLET (MySQL)
// =============================
function obtenerProductos() {
    // Agregamos un timestamp para evitar que el navegador use caché vieja
    fetch(`http://localhost:8080/api-readycar/api/productos?t=${new Date().getTime()}`)
        .then(response => {
            if (!response.ok) throw new Error("Error al conectar con el servidor Java");
            return response.json();
        })
        .then(data => {
            console.log("Datos recibidos de MySQL:", data);
            mostrarProductos(data);
        })
        .catch(error => {
            console.error("Hubo un problema:", error);
            if(listaTodos) listaTodos.innerHTML = "<p style='color:red;'>Error al cargar productos.</p>";
        });
}

// =============================
// 4. DIBUJAR PRODUCTOS EN PANTALLA
// =============================
function mostrarProductos(productos) {
    if(!listaDestacados || !listaTodos) return;

    listaDestacados.innerHTML = "";
    listaTodos.innerHTML = "";

    productos.forEach(producto => {
        
        const { id, nombre, precio, descripcion, imagen, destacado } = producto;

        const div = document.createElement('div');
        div.classList.add('box');

        
        const precioFormateado = new Intl.NumberFormat('es-CO', {
            style: 'currency',
            currency: 'COP',
            minimumFractionDigits: 0
        }).format(precio);

        div.innerHTML = `
            <img src="images/${imagen}" alt="${nombre}" onerror="this.src='images/default.jpg'">
            <div class="product-txt">
                <h3>${nombre}</h3>
                <p>${descripcion}</p>
                <p class="precio">${precioFormateado}</p>
                <a href="#" class="agregar-carrito btn-2" data-id="${id}">Agregar al carrito</a>
            </div>
        `;

        if (destacado == 1 || destacado === true) {
            listaDestacados.appendChild(div);
        } else {
            listaTodos.appendChild(div);
        }
    });
}

// =============================
// 5. EVENTOS
// =============================
function cargarEventListeners() {
    // Escuchar clicks en ambas listas
    if (listaDestacados) listaDestacados.addEventListener('click', agregarProducto);
    if (listaTodos) listaTodos.addEventListener('click', agregarProducto);
    
    if (carrito) carrito.addEventListener('click', eliminarProducto);
    
    if (vaciarCarritoBtn) {
        vaciarCarritoBtn.addEventListener('click', (e) => {
            e.preventDefault();
            articulosCarrito = [];
            limpiarCarritoHTML();
        });
    }
}

function agregarProducto(e) {
    e.preventDefault();
    if (e.target.classList.contains('agregar-carrito')) {
        const productoCard = e.target.closest('.box');
        leerDatosProducto(productoCard);
    }
}

function eliminarProducto(e) {
    e.preventDefault();
    if (e.target.classList.contains('borrar-producto')) {
        const productoId = e.target.getAttribute('data-id');
        articulosCarrito = articulosCarrito.filter(p => p.id !== productoId);
        carritoHTML();
    }
}

// =============================
// 6. LÓGICA DEL CARRITO
// =============================
function leerDatosProducto(producto) {
    const infoProducto = {
        imagen: producto.querySelector('img').src,
        titulo: producto.querySelector('h3').textContent,
        precio: producto.querySelector('.precio').textContent,
        id: producto.querySelector('.agregar-carrito').getAttribute('data-id'),
        cantidad: 1
    };

    const existe = articulosCarrito.some(p => p.id === infoProducto.id);

    if (existe) {
        articulosCarrito = articulosCarrito.map(p => {
            if (p.id === infoProducto.id) {
                p.cantidad++;
                return p;
            } else {
                return p;
            }
        });
    } else {
        articulosCarrito = [...articulosCarrito, infoProducto];
    }
    carritoHTML();
}

function carritoHTML() {
    limpiarCarritoHTML();
    articulosCarrito.forEach(p => {
        const { imagen, titulo, precio, cantidad, id } = p;
        const row = document.createElement('tr');
        row.innerHTML = `
            <td><img src="${imagen}" width="50"></td>
            <td>${titulo}</td>
            <td>${precio}</td>
            <td>${cantidad}</td>
            <td><a href="#" class="borrar-producto" data-id="${id}" style="color:red; font-weight:bold; text-decoration:none;">X</a></td>
        `;
        contenedorCarrito.appendChild(row);
    });
}

function limpiarCarritoHTML() {
    while (contenedorCarrito.firstChild) {
        contenedorCarrito.removeChild(contenedorCarrito.firstChild);
    }
}
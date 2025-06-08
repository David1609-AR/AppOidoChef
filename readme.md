# 🍽️ OidoChef - Sistema de Gestión de Pedidos

Aplicación multiplataforma para gestionar pedidos en un restaurante. Consta de dos partes:

* 📱 **App Android (Camareros)**: permite seleccionar productos por mesa, crear pedidos y enviarlos a cocina.
* 💻 **App Escritorio JavaFX (Cocina/Administración)**: recibe pedidos en tiempo real, gestiona mesas, productos, reservas y usuarios.

---

## 🚀 Características principales

* 📟 Gestión de pedidos por mesa
* 📱 WebSocket en tiempo real (notificaciones de pedidos)
* 🍔 Productos agrupados por categorías
* 👨‍🍳 Envío de pedidos a cocina
* 📋 Reservas por fecha y mesa
* 🔐 Login de usuarios (JavaFX)
* 📊 Panel visual de mesas en JavaFX

---

## ⚙️ Tecnologías

| Plataforma    | Tecnología           |
| Backend       | Java + Spark REST    |
| Escritorio    | JavaFX + FXML        |
| Android       | Java                 |
| Comunicación  | WebSocket            |
| Base de Datos | MySQL                |

---

## ⚙️ Requisitos

* Java 17+
* MySQL Server (usuario: `root`, contraseña: `1234`)
* Maven
* Android Studio (para compilar la app móvil)

---

## 🏁 Cómo ejecutar

### 👤 App JavaFX

1. Abre el proyecto en IntelliJ o VSCode.
2. Ejecuta `Main.java`:

    * Inicia el servidor REST (`RestServer.init()`).
    * Se conecta al WebSocket.
3. Interfaz disponible con mesas, botones, reservas y productos.

### 📱 App Android

1. Abre el proyecto en Android Studio.
2. Asegúrate de que el IP en `WebSocketListenerService` apunte al servidor JavaFX.
3. Ejecuta en emulador o dispositivo.
4. Selecciona una mesa, elige productos → Enviar pedido.

---

## 📱 WebSocket

* El servidor WebSocket está en `PedidoWebSocketHandler.java`
* El cliente en Android (`WebSocketListenerService`) y JavaFX (`PedidoWebSocketClientFX`)
* URI por defecto: `ws://192.168.1.16:4567/ws/pedidos`

🔧 Cambia la IP según tu red local si es necesario.

---

## 👤 Usuarios

* Los usuarios se gestionan desde la app JavaFX.
* Incluye login y CRUD (crear, ver, eliminar usuarios).

---

### Tablas clave:

* `productos`
* `pedido` y `items_pedido`
* `mesas`
* `usuarios`
* `reservas`

---

## 🧪 Estado del proyecto

* ✅ Pedido por mesa en Android
* ✅ Envío por WebSocket
* ✅ Vista dinámica de mesas JavaFX
* ✅ WebSocket receptor en cocina

---
## 📩 Contacto

Desarrollo de Aplicaciones Multiplataforma
Autor: David Aramayo Ramirez
📧 Email: aramayodavid85@gmail.com 

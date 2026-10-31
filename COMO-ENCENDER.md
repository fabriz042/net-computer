# Cómo encender el sistema Net Computer

## Qué es cada parte

| Parte | Qué hace | Dónde está |
|---|---|---|
| **Base de datos** | Guarda productos, usuarios, pedidos (PostgreSQL) | Corre dentro de Docker |
| **Backend** | Programa en Java que procesa la información | Carpeta `back-main` |
| **Frontend** | La página web que ves en el navegador (TypeScript) | Carpeta `front-main` |

Las 3 partes tienen que estar encendidas al mismo tiempo.

---

## ANTES DE EMPEZAR

Abre **Docker Desktop** desde el menú Inicio y espera 1 minuto a que cargue.
No tienes que hacer nada dentro de Docker, solo dejarlo abierto.

---

## PARTE 1: Abrir la terminal en VS Code

1. Abre **VS Code**.
2. Si no aparece el proyecto: menú **Archivo** → **Abrir carpeta** → elige
   `Escritorio\Proyectos\net-computer\net-computer-main`.
3. Menú de arriba: **Terminal** → **Nuevo terminal**.
4. Abajo se abre un cuadro negro. Ahí se pegan los comandos.

Para pegar: clic dentro del cuadro negro → **Ctrl + V** → **Enter**.

---

## PARTE 2: Encender la base de datos

Pega este comando y pulsa Enter:

```powershell
cd C:\Users\Pablo\Desktop\Proyectos\net-computer\net-computer-main
```

Pega este otro y pulsa Enter:

```powershell
docker compose -f docker-compose.local.yml --env-file .env up -d
```

Debe decir `Running` o `Started`.

---

## PARTE 3: Encender el backend (Java)

En la misma terminal, pega y pulsa Enter:

```powershell
cd back-main
```

Pega y pulsa Enter:

```powershell
.\gradlew.bat bootRun
```

Espera 1 o 2 minutos. Está listo cuando aparece una línea con la palabra **Started**.

**No cierres esta terminal.**

---

## PARTE 4: Abrir una segunda terminal

Menú de arriba: **Terminal** → **Nuevo terminal**.

---

## PARTE 5: Encender el frontend (página web)

En la terminal nueva, pega y pulsa Enter:

```powershell
cd C:\Users\Pablo\Desktop\Proyectos\net-computer\net-computer-main\front-main
```

Pega y pulsa Enter:

```powershell
npm run dev
```

Está listo cuando aparece **Ready**.

**Tampoco cierres esta terminal.**

---

## PARTE 6: Ver el sistema

Abre Chrome o Edge y escribe en la barra de direcciones:

```
localhost:3000
```

Pulsa Enter.

---

## Para APAGAR todo

En cada terminal: clic dentro del cuadro negro → **Ctrl + C**.
Si pregunta algo, escribe `S` y pulsa Enter.

---

## Errores comunes

| Mensaje | Qué hacer |
|---|---|
| `failed to connect to the docker API` | Docker Desktop está cerrado. Ábrelo, espera 1 minuto y repite la PARTE 2. |
| `Port 4001 was already in use` | El backend ya está encendido en otra terminal. Ciérrala o usa esa. |
| `Port 3000 is in use` | El frontend ya está encendido en otra terminal. Ciérrala o usa esa. |
| `'gradlew.bat' no se reconoce` | No estás en la carpeta `back-main`. Repite el `cd` de la PARTE 3. |
| `Missing script: "dev"` | No estás en la carpeta `front-main`. Repite el `cd` de la PARTE 5. |

---

## Direcciones

- Página web: http://localhost:3000
- Backend (API): http://localhost:4001/api

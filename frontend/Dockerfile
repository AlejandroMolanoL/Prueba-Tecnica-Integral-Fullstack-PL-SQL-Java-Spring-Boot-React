# Etapa 1: Construcción de la aplicación con Node.js
FROM node:22-alpine AS build
WORKDIR /app

# Copiar manifiestos de dependencias
COPY package.json yarn.lock ./

# Instalar dependencias
RUN yarn install --frozen-lockfile || yarn install

# Copiar código fuente
COPY . .

# Compilar para producción
RUN yarn build

# Etapa 2: Servidor web ultraligero con Nginx
FROM nginx:alpine

# Copiar configuración personalizada de Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copiar bundle compilado desde la etapa de build
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

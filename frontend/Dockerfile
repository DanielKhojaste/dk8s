# syntax=docker/dockerfile:1
# Built from the repository root (see compose.yaml), because vite.config.ts reads ../VERSION.

# ---- Stage 1: build the static site ----
FROM node:22-alpine AS build
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY VERSION /app/VERSION
COPY frontend/ ./
RUN npm run build

# ---- Stage 2: serve it with nginx, which also forwards /api to control-api ----
FROM nginx:1.29-alpine
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/frontend/dist /usr/share/nginx/html
EXPOSE 80

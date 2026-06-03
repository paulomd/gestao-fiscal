FROM node:16-alpine AS build
ARG KEYCLOAK_URL=http://localhost:8180
ARG JSF_BASE_URL=http://localhost
WORKDIR /app
COPY frontend-angular/package.json frontend-angular/package-lock.json frontend-angular/.npmrc frontend-angular/angular.json frontend-angular/tsconfig*.json ./
RUN npm ci --legacy-peer-deps && rm -rf node_modules/@types/node
COPY frontend-angular/src ./src
COPY shared/theme/corporate.css ./src/assets/theme/corporate.css
RUN sed -i "s|http://localhost:8180|${KEYCLOAK_URL}|g" src/environments/environment.prod.ts \
 && sed -i "s|jsfBaseUrl: 'http://localhost'|jsfBaseUrl: '${JSF_BASE_URL}'|g" src/environments/environment.prod.ts \
 && npm run build

FROM nginx:1.27-alpine
COPY --from=build /app/dist/gestao-fiscal-angular /usr/share/nginx/html
COPY frontend-angular/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80

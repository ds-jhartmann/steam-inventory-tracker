# Build App
FROM node:18 AS node
WORKDIR /app
COPY . .
RUN npm install
RUN npm run build --prod

# Bundle as image
FROM nginx:alpine
COPY --from=node /app/dist/tracker-frontend /usr/share/nginx/html

# Stage 1: Build the Vue.js application
FROM node:lts-alpine as build

# Set build-time arguments
ARG VITE_AUTH_URL
ARG VITE_CORE_URL

# Set environment variables from build-time arguments
ENV VITE_AUTH_URL=${VITE_AUTH_URL}
ENV VITE_CORE_URL=${VITE_CORE_URL}

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Stage 2: Serve the application with Nginx
FROM nginx:stable-alpine as production
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

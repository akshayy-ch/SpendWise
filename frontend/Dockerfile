# Stage 1: Build the React application
FROM node:22-alpine AS build

WORKDIR /app

# Copy dependency manifests first
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy the remaining frontend source
COPY . .

# Generate the production build
RUN npm run build


# Stage 2: Serve the application using Nginx
FROM nginx:alpine

# Remove the default Nginx website
RUN rm -rf /usr/share/nginx/html/*

# Copy the production build into Nginx
COPY --from=build /app/dist /usr/share/nginx/html

# Add our Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY cors_preflight.conf /etc/nginx/cors_preflight.conf
COPY cors_headers.conf /etc/nginx/cors_headers.conf
EXPOSE 8090
CMD ["nginx", "-g", "daemon off;"]

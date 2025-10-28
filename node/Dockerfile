# --- Build stage ---
FROM node:18-alpine AS build
WORKDIR /app

# Install deps (leverage Docker layer caching)
COPY package*.json ./
RUN npm ci

# Build
COPY . .
RUN npm run build

# --- Runtime stage ---
FROM node:18-alpine AS runtime
WORKDIR /app

ENV NODE_ENV=production
# If you use next 13/14 app dir, this keeps the minimal files:
COPY --from=build /app/package*.json ./
RUN npm ci --omit=dev

# Next.js output and public assets
COPY --from=build /app/.next ./.next
COPY --from=build /app/public ./public
# If you have next.config.js, copy it too (optional)
COPY --from=build /app/next.config.js ./ 2>/dev/null || true

# Expose and start
EXPOSE 3000
CMD ["npm", "run", "start"]

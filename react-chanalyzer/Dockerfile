# Use an official Node.js runtime as the base image
FROM node:alpine
# Set the working directory in the container
WORKDIR /app
# Copy package.json and package-lock.json to the container
COPY package*.json ./
COPY entrypoint.sh /
# Install dependencies
RUN npm install
# Copy the rest of the application code to the container
COPY . .
# Build the React app
RUN npm run build
EXPOSE 3000
# Set the command to run the production build of the React app
ENTRYPOINT ["sh", "../entrypoint.sh"]
# Use an official Node.js image as base
FROM node:18

# Create app directory
WORKDIR /usr/src/app

# Install app dependencies
COPY package*.json ./
RUN npm install

# Bundle app source
COPY . .

# Expose port (match the one your app uses)
EXPOSE 2884

# Start the app
CMD ["npm", "start"]
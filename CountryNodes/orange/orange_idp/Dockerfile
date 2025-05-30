# Use an official Node.js image as base
FROM node:18

# Create app directory
WORKDIR /usr/src/app

# Install app dependencies
COPY package*.json ./
RUN npm install


RUN npm run build

# Bundle app source
COPY . .

# Expose port (match the one your app uses)
EXPOSE 3000

# Start the app
CMD ["npm", "start"]
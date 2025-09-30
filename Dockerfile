# escape=\
# syntax=docker/dockerfile:1

FROM openjdk:21-jdk-slim

# Create required directories
RUN mkdir -p /bot/plugins
RUN mkdir -p /bot/data
RUN mkdir -p /dist/out

# Declare required volumes
VOLUME [ "/bot/data" ]
VOLUME [ "/bot/plugins" ]

# Copy the distribution files into the container
COPY [ "build/distributions/allium-1.0.4.tar", "/dist" ]

# Extract the distribution files, and prepare them for use
RUN tar -xf /dist/allium-1.0.4.tar -C /dist/out
RUN chmod +x /dist/out/allium-1.0.4/bin/allium

# Clean up unnecessary files
RUN rm /dist/allium-1.0.4.tar

# Set the correct working directory
WORKDIR /bot

# Run the distribution start script
ENTRYPOINT [ "/dist/out/allium-1.0.4/bin/allium" ]

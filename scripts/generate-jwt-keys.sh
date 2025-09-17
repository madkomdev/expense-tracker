#!/bin/bash

# generate-jwt-keys.sh
# Script to generate RSA key pair for JWT signing in production

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo ""

# Create keys directory if it doesn't exist
KEYS_DIR="./keys"
mkdir -p "$KEYS_DIR"

# Generate private key
echo -e "${GREEN}1. Generating RSA private key (2048-bit)...${NC}"
openssl genrsa -out "$KEYS_DIR/jwt-private.pem" 2048

# Generate public key from private key
echo -e "${GREEN}2. Extracting public key...${NC}"
openssl rsa -in "$KEYS_DIR/jwt-private.pem" -pubout -out "$KEYS_DIR/jwt-public.pem"

# Convert private key to PKCS#8 format (required by Java)
echo -e "${GREEN}3. Converting private key to PKCS#8 format...${NC}"
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in "$KEYS_DIR/jwt-private.pem" -out "$KEYS_DIR/jwt-private-pkcs8.pem"

# Base64 encode the keys for environment variables
echo -e "${GREEN}4. Encoding keys to Base64...${NC}"

PRIVATE_KEY_BASE64=$(cat "$KEYS_DIR/jwt-private-pkcs8.pem" | grep -v "BEGIN\|END" | tr -d '\n')
PUBLIC_KEY_BASE64=$(cat "$KEYS_DIR/jwt-public.pem" | grep -v "BEGIN\|END" | tr -d '\n')

# Generate a unique key ID with timestamp
KEY_ID="expense-tracker-$(date +%Y%m%d-%H%M%S)"

echo ""
echo -e "${BLUE}=== JWT Keys Generated Successfully ===${NC}"
echo ""
echo -e "${YELLOW}Copy the following environment variables to your production configuration:${NC}"
echo ""

# Create .env.prod file
ENV_FILE="$KEYS_DIR/.env.prod"
cat > "$ENV_FILE" << EOF
# JWT Configuration for Production
# Generated on $(date)
JWT_PRIVATE_KEY=$PRIVATE_KEY_BASE64
JWT_PUBLIC_KEY=$PUBLIC_KEY_BASE64
JWT_KEY_ID=$KEY_ID
JWT_ISSUER=expense-tracker-production
JWT_EXPIRATION=900
EOF

echo -e "${GREEN}Environment file created: ${ENV_FILE}${NC}"
echo ""

# Security reminders
echo -e "${RED}SECURITY REMINDERS:${NC}"
echo -e "${RED}1. Store these keys securely (use a password manager or secrets vault)${NC}"
echo -e "${RED}2. Never commit these keys to version control${NC}"
echo -e "${RED}3. Use different keys for each environment (dev, staging, prod)${NC}"
echo -e "${RED}4. Rotate keys regularly (recommended: every 6-12 months)${NC}"
echo -e "${RED}5. Delete the generated .pem files after copying the environment variables${NC}"
echo ""

# Display key information
echo -e "${BLUE}Key Information:${NC}"
echo "Key ID: $KEY_ID"
echo "Algorithm: RS256"
echo "Key Size: 2048 bits"
echo "Generated: $(date)"
echo ""

echo -e "${GREEN}Key generation completed successfully!${NC}"
echo -e "${YELLOW}Remember to update your production environment with these values.${NC}"

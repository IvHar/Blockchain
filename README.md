# Blockchain Simulation Project

## Overview
This project simulates a basic blockchain with mining, transaction handling, and block validation
It includes key components such as miners which create new blocks, transactions that transfer *virtual coins*,
and the blockchain itself that stores and validates blocks

## Features
- **Miners**: Simulate mining by finding hash which starts with a dynamic number of '0' to create new blocks
- **Transactions**: Is used for sending virtual currency between miners
- Blockchain: Stores blocks in sequence where new block stores hash of previous one, with features like difficulty adjustment
- Security: Transactions are digitally signed and validated, ensuring data integrity

## Project Structure

### 1. Main
The entry point for the application,
sets up the blockchain, loads existing blocks, creates a pool of miners,
and starts submitting transactions for mining

### 2. Miner
Represents a miner in the blockchain network:
- Mines new blocks by solving a cryptographic puzzle
- Transfers virtual currency to other miners

#### Key Methods:
- call(): Method for mining new block, mines it by finding a valid hash
- run(): Method for sending currency transfers to other miners
- reward(): Increases the miner's virtual coin balance by 100 VC if a new block was created by miner

### 3. Block
Represents a single block in the blockchain.
Each block contains a hash, previous block's hash, timestamp, and transaction data.
It also includes a magic number used in the mining process

### 4. Transaction
Represents a transaction between two miners.
Every transaction has a sender, receiver, transfer amount, signature, and unique identifier

### 5. Blockchain
Handles the blockchain logic, including validation:
- Manages the list of blocks and miners
- Validates new blocks before adding them into blockchain
- Sets the mining difficulty based on block generation time

#### Key Methods:
- addBlock(): Adds a valid block to the blockchain and saves it. The block is valid if it contains the hash of previous block.
- setDifficulty(): Sets the difficulty based on how long it takes to mine a block.

### 6. TransactionService
Handles the creation and validation of transactions:
- Allows miners to send virtual currency to one another
- Validates transactions in a block

#### Key Methods:
- sendTransfer(): Creates a transaction and adds it to the pool
- validateTransactions(): Ensures that all transactions are valid

### 7. HashService.java
Provides cryptographic services, including hashing for block mining. Uses SHA-256 hashing and a magic number to mine a block.

### 8. FileService.java
Handles loading and saving the blockchain and metadata (block difficulty):
- Saves blocks to disk as serialized files.
- Loads the blockchain and difficulty settings when the program starts.

## How It Works
1. Blockchain Initialization:
    - When the application starts, it loads the existing blockchain and transactions from files

2. Mining:
    - A pool of miners is created, each mines blocks by finding a hash with a specific number of leading zeros
    - Once a valid hash is found, the miner submits the block to the blockchain

3. Transaction Handling:
    - Miners can send virtual coins to each other by creating transactions, which are signed with the sender's private key
    - These transactions are validated and stored in the blocks

4. Difficulty Adjustment:
    - If the block is mined too fast, the mining difficulty increases. and if it's mined too slow, the difficulty decreases

## Files and Folders
- src/: Contains all Java source code.
- blockchain_blocks/: Directory where blockchain blocks are saved.
- transactions.dat: Stores transaction data.
- identifier.dat: Stores the unique transaction identifier.
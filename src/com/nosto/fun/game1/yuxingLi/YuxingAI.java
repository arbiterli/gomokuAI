package com.nosto.fun.game1.yuxingLi;

import java.util.ArrayList;
import java.util.List;

import com.nosto.fun.game1.ArenaPosition;
import com.nosto.fun.game1.Piece;
import com.nosto.fun.game1.Player;

public class YuxingAI implements Player, Cloneable {
	private Piece myPiece;
	private String name;
	private int maxDepth; // decide how deep to go in the search tree.

	public YuxingAI(String name, int maxDepth) {
		this.name = name;
		this.maxDepth = maxDepth;
	}

	public void setSide(Piece p) {
		myPiece = p;
	}

	public Piece getSide() {
		return myPiece;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public String toString() {
		return getName();
	}

	public ArenaPosition move(Piece[][] board, ArenaPosition lastPosition) {
		ArenaPosition nextMove = null;
		int maxUtility = Integer.MIN_VALUE;

		// go through board, using minimax search algorithm to get max utility
		// and corresponding move.
		List<ArenaPosition> candidate = generateCadidate(board, lastPosition);
		for (ArenaPosition position : candidate) {
			int i = position.getRow();
			int j = position.getColumn();
			if (board[i][j] == null) {
				board[i][j] = myPiece;
				int utility = minimax(board, Integer.MIN_VALUE,
						Integer.MAX_VALUE, theOtherPiece(myPiece), 1,
						new ArenaPosition(i, j));
				if (utility > maxUtility) {
					maxUtility = utility;
					nextMove = new ArenaPosition(i, j);
				}
				board[i][j] = null;
			}
		}
		return nextMove;
	}

	/**
	 * minimax search with alpha beta pruning.
	 */
	private int minimax(Piece[][] board, int alpha, int beta,
			Piece currentPiece, int depth, ArenaPosition lastPosition) {
		if (depth == this.maxDepth) {
			return calUtility(board);
		}
		int utility = 0;
		List<ArenaPosition> candidate = generateCadidate(board, lastPosition);
		// my turn to maximize utility.
		if (myPiece == currentPiece) {
			utility = alpha;
			for (ArenaPosition position : candidate) {
				int i = position.getRow();
				int j = position.getColumn();
				if (board[i][j] == null) {
					board[i][j] = currentPiece;
					int value = minimax(board, utility, beta,
							theOtherPiece(currentPiece), depth + 1,
							new ArenaPosition(i, j));
					utility = Math.max(value, utility);
					board[i][j] = null; // undo this move.
					if (utility >= beta) { // cut brunch
						break;
					}
				}

			}
		} else { // opponent's turn who is going to minimize utility.
			utility = beta;
			for (ArenaPosition position : candidate) {
				int i = position.getRow();
				int j = position.getColumn();
				if (board[i][j] == null) {
					board[i][j] = currentPiece;
					int value = minimax(board, alpha, utility,
							theOtherPiece(currentPiece), depth + 1,
							new ArenaPosition(i, j));
					utility = Math.min(value, utility);
					board[i][j] = null;
					if (utility <= alpha) {
						break;
					}
				}
			}
		}
		return utility;
	}

	/**
	 * generate a list for candidate moves with an order that moves near to last
	 * move come first. Since search order matters a lot for alpha beta pruning.
	 */
	private List<ArenaPosition> generateCadidate(Piece[][] board,
			ArenaPosition lastPosition) {
		List<ArenaPosition> candi = new ArrayList<ArenaPosition>();
		// find the smallest rectangle which contains all previous moves.
		int bottom = 0, up = board.length, right = 0, left = board.length;

		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board.length; ++j) {
				if (board[i][j] != null) {
					if (i > bottom) {
						bottom = i;
					}
					if (i < up) {
						up = i;
					}
					if (j > right) {
						right = j;
					}
					if (j < left) {
						left = j;
					}
				}
			}
		}
		// basically, search range is a little bigger than the rectangle which
		// contains all previous moves
		bottom = Math.min(bottom + 1, board.length - 1);
		up = Math.max(up - 1, 0);
		right = Math.min(right + 1, board.length - 1);
		left = Math.max(left - 1, 0);

		int distance = 1;
		int startX = lastPosition.getRow();
		int startY = lastPosition.getColumn();
		while (true) {
			int breakCondition = 0;
			// downside
			if (startX + distance <= bottom) {
				for (int i = Math.max(startY - distance, left); i <= Math.min(
						startY + distance, right); ++i) {
					if (board[startX + distance][i] == null) {
						candi.add(new ArenaPosition(startX + distance, i));
					}
				}
			} else {
				breakCondition++;
			}
			// upside
			if (startX - distance >= up) {
				for (int i = Math.max(startY - distance, left); i <= Math.min(
						startY + distance, right); ++i) {
					if (board[startX - distance][i] == null) {
						candi.add(new ArenaPosition(startX - distance, i));
					}
				}
			} else {
				breakCondition++;
			}
			// rightside
			if (startY + distance <= right) {
				for (int i = Math.max(startX - distance + 1, up); i <= Math
						.min(startX + distance - 1, bottom); ++i) {
					if (board[i][startY + distance] == null) {
						candi.add(new ArenaPosition(i, startY + distance));
					}
				}
			} else {
				breakCondition++;
			}
			// leftside
			if (startY - distance >= left) {
				for (int i = Math.max(startX - distance + 1, up); i <= Math
						.min(startX + distance - 1, bottom); ++i) {
					if (board[i][startY - distance] == null) {
						candi.add(new ArenaPosition(i, startY - distance));
					}
				}
			} else {
				breakCondition++;
			}

			if (breakCondition == 4) { // all four direction reach the bound.
				break;
			}
			distance++;
		}
		return candi;
	}

	/**
	 * get another side.
	 */
	private Piece theOtherPiece(Piece piece) {
		if (piece == Piece.CROSS) {
			return Piece.ROUND;
		} else {
			return Piece.CROSS;
		}
	}

	/**
	 * evaluate the utility of current board. calculate utility row by row,
	 * column by column, diagonal by diagonal.
	 */
	private int calUtility(Piece[][] board) {
		int utility = 0;
		Piece[] row = null;
		Piece[] column = null;
		Piece[] diagonal1 = null; // lower part of top-left to bottom-right
									// diagonal
		Piece[] diagonal2 = null; // upper part of top-left to bottom-right
									// diagonal
		Piece[] diagonal3 = null; // lower part of top-right to bottom-left
									// diagonal
		Piece[] diagonal4 = null; // upper part of top-right to bottom-left
									// diagonal
		for (int i = 0; i < board.length; ++i) {
			row = board[i];
			column = new Piece[board.length];
			for (int j = 0; j < board.length; ++j) {
				column[j] = board[j][i];
			}

			diagonal1 = new Piece[board.length - i];
			for (int j = 0; j < diagonal1.length; ++j) {
				diagonal1[j] = board[i + j][j];
			}

			diagonal3 = new Piece[board.length - i];
			for (int j = 0; j < diagonal3.length; ++j) {
				diagonal3[j] = board[j][board.length - j - i - 1];
			}

			if (i > 0) {
				diagonal2 = new Piece[board.length - i];
				for (int j = 0; j < diagonal2.length; ++j) {
					diagonal2[j] = board[j][i + j];
				}

				diagonal4 = new Piece[board.length - i];
				for (int j = 0; j < diagonal4.length; ++j) {
					diagonal4[j] = board[i + j][board.length - 1 - j];
				}
			}
			utility += lineUtility(row) + lineUtility(column)
					+ lineUtility(diagonal1) + lineUtility(diagonal2)
					+ lineUtility(diagonal3) + lineUtility(diagonal4);
		}
		return utility;
	}

	/**
	 * calculate utility of one line: maybe row, column or diagonal.
	 */
	private int lineUtility(Piece[] line) {
		if (line == null || line.length < 5) {
			return 0;
		}
		int utility = 0;

		Piece piece = null;
		int i = 0; // start position.
		// count continuous number and block number for segment with consecutive
		// pieces
		while (i < line.length - 1) {
			if (line[i] != null) {
				int continuousNumber = 0;
				int blockNumber = 0;
				piece = line[i];
				continuousNumber = 0;
				blockNumber = 0;
				if (i == 0 || line[i - 1] == theOtherPiece(piece)) {
					blockNumber++;
				}
				int j = i;
				while (j < line.length - 1) {
					if (line[j] == piece) {
						continuousNumber++;
						j++;
					} else {
						break;
					}
				}
				i = j;
				if (i == line.length - 1 || line[i] == theOtherPiece(piece)) {
					blockNumber++;
				}
				utility += segmentUtility(continuousNumber, blockNumber, piece);
			} else {
				++i;
			}
		}
		return utility;
	}

	/**
	 * calculate utility of a continuous segment.
	 */
	private int segmentUtility(int continuousNumber, int blockNumber,
			Piece piece) {
		int value = 0;

		if (continuousNumber == 5) {
			value = 1000000000; // it is better to make this constant number a
								// enum type if we are going to extend the code.
								// Here I do not want to make code too large.
		} else {
			if (blockNumber == 2) { // blocked on both side.
				value = 0;
			} else if (blockNumber == 1) { // blocked on one side
				if (continuousNumber == 4) {
					value = 100000;
				} else if (continuousNumber == 3) {
					value = 1000;
				} else if (continuousNumber == 2) {
					value = 10;
				}
			} else if (blockNumber == 0) { // not blocked
				if (continuousNumber == 4) {
					value = 100000000;
				} else if (continuousNumber == 3) {
					value = 100000;
				} else if (continuousNumber == 2) {
					value = 1000;
				}
			}
		}

		value = (piece == myPiece) ? value : -value * 2; // times 2 means more
															// likely to block
															// opponent.
		return value + (int) (Math.random() * 10); // add some randomness to
													// avoid same move.
	}
}

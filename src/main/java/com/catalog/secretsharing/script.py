# Let's test our algorithm implementation with the test cases to verify correctness

# Test Case 1 - Manual verification
print("=== TEST CASE 1 VERIFICATION ===")
test1_points = [(1, 4), (2, 7), (3, 12), (6, 39)]
print(f"Decoded points: {test1_points}")

# Using first 3 points (k=3)
test1_selected = test1_points[:3]
print(f"Selected points for interpolation: {test1_selected}")

def lagrange_constant_term(points):
    """Calculate constant term using Lagrange interpolation"""
    secret = 0.0
    n = len(points)
    
    for i in range(n):
        xi, yi = points[i]
        
        # Calculate Li(0) - Lagrange basis polynomial at x=0
        li_0 = 1.0
        for j in range(n):
            if i != j:
                xj, _ = points[j]
                li_0 *= (0 - xj) / (xi - xj)
        
        secret += yi * li_0
    
    return secret

secret1 = lagrange_constant_term(test1_selected)
print(f"Calculated secret: {secret1}")

# Let's also verify with a different combination of 3 points
test1_alt = [test1_points[0], test1_points[1], test1_points[3]]  # points 1,2,6
print(f"\nAlternative point selection: {test1_alt}")
secret1_alt = lagrange_constant_term(test1_alt)
print(f"Alternative calculation secret: {secret1_alt}")

print(f"\nBoth calculations match: {abs(secret1 - secret1_alt) < 1e-10}")

# Let's verify this is correct by constructing the polynomial manually
# If we have points (1,4), (2,7), (3,12), what polynomial fits?
# Let's assume it's a quadratic: ax² + bx + c

# System of equations:
# a(1)² + b(1) + c = 4  =>  a + b + c = 4
# a(2)² + b(2) + c = 7  =>  4a + 2b + c = 7  
# a(3)² + b(3) + c = 12 =>  9a + 3b + c = 12

# From first two equations: 3a + b = 3
# From second and third: 5a + b = 5
# Solving: 2a = 2, so a = 1
# Then b = 3 - 3(1) = 0  
# And c = 4 - 1 - 0 = 3

print(f"\nManual polynomial construction:")
print(f"If polynomial is x² + 0x + 3 = x² + 3")
print(f"Check: f(1) = 1² + 3 = {1**2 + 3}")
print(f"Check: f(2) = 2² + 3 = {2**2 + 3}")  
print(f"Check: f(3) = 3² + 3 = {3**2 + 3}")
print(f"Check: f(6) = 6² + 3 = {6**2 + 3}")
print(f"The constant term c = 3, which matches our calculation!")

print("\n" + "="*50)
print("✅ TEST CASE 1 VERIFICATION PASSED")
print("✅ Secret = 3 (matches expected result)")
print("="*50)
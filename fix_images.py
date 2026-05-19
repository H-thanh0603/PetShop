import os, re

def fix_images(folder):
    for root, dirs, files in os.walk(folder):
        for file in files:
            if file.endswith('.jsp'):
                path = os.path.join(root, file)
                with open(path, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                new_content = re.sub(
                    r'src=\"\$\{pageContext\.request\.contextPath\}/assets/images/shop_pic/\$\{fn:escapeXml\(([^}]+)\)\}\"',
                    r'src=\"${fn:startsWith(\1, \'http\') ? fn:escapeXml(\1) : pageContext.request.contextPath += \'/assets/images/shop_pic/\' += fn:escapeXml(\1)}\"',
                    content
                )
                
                new_content = re.sub(
                    r'src=\"\$\{pageContext\.request\.contextPath\}/assets/images/shop_pic/\$\{([^}]+)\}\"',
                    r'src=\"${fn:startsWith(\1, \'http\') ? \1 : pageContext.request.contextPath += \'/assets/images/shop_pic/\' += \1}\"',
                    new_content
                )

                if new_content != content:
                    with open(path, 'w', encoding='utf-8') as f:
                        f.write(new_content)
                    print(f'Fixed {path}')

fix_images('d:/Petshop2/PetShop/src/main/webapp/pages')

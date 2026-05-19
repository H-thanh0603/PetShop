import os

def cleanup(folder):
    for root, dirs, files in os.walk(folder):
        for file in files:
            if file.endswith('.jsp'):
                path = os.path.join(root, file)
                with open(path, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                # Replace literal backslashes that were mistakenly added
                new_content = content.replace('src=\\"${', 'src="${')
                new_content = new_content.replace('}\\"', '}"')
                new_content = new_content.replace("\\'http\\'", "'http'")
                new_content = new_content.replace("\\'/assets/images/shop_pic/\\'", "'/assets/images/shop_pic/'")
                
                if new_content != content:
                    with open(path, 'w', encoding='utf-8') as f:
                        f.write(new_content)
                    print(f'Cleaned up {path}')

cleanup('d:/Petshop2/PetShop/src/main/webapp/pages')
